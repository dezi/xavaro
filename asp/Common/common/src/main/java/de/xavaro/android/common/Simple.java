package de.xavaro.android.common;

import android.app.Application;
import android.support.annotation.Nullable;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.provider.Settings;
import android.app.Activity;
import android.app.NotificationManager;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.ResolveInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.view.SoundEffectConstants;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.DhcpInfo;
import android.net.Uri;
import android.os.Vibrator;
import android.os.Handler;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Simple
{
    private static final String LOGTAG = Simple.class.getSimpleName();

    //region Initialisation

    private static Activity actContext;
    private static Context appContext;
    private static Context anyContext;
    private static Handler appHandler;
    private static WifiManager wifiManager;

    public static void setAppContext(Context context)
    {
        appContext = context;
        anyContext = context;
        appHandler = new Handler();
    }

    public static Context getAppContext()
    {
        return appContext;
    }

    public static void setActContext(Activity context)
    {
        actContext = context;
    }

    public static Activity getActContext()
    {
        return actContext;
    }

    public static Context getAnyContext()
    {
        return appContext;
    }

    public static Resources getResources()
    {
        return appContext.getResources();
    }

    public static Object getSystemService(String service)
    {
        return anyContext.getSystemService(service);
    }

    public static ContentResolver getContentResolver()
    {
        return appContext.getContentResolver();
    }

    public static int getIdentifier(String name, String type, String pack)
    {
        return getResources().getIdentifier(name, type, pack);
    }

    //endregion Initialisation

    //region GCM stuff

    public static boolean isGCMInitialized()
    {
        return (getGCMapeyki() != null);
    }

    @Nullable
    public static String getGCMToken()
    {
        return CommonStatic.gcm_token;
    }

    public static long getGCMsendeird()
    {
        if (anyContext != null)
        {
            if (anyContext.getPackageName().equals("de.xavaro.android.safehome"))
            {
                return 2955796475946577539L;
            }
        }

        return 0;
    }

    @Nullable
    public static String getGCMapeyki()
    {
        if (anyContext != null)
        {
            if (anyContext.getPackageName().equals("de.xavaro.android.safehome"))
            {
                return "HLscZ|HCQc]g}nZG=MKu~3H0?b?7sm1wp_kPcn=";
            }
        }

        return null;
    }

    //endregion GCM stuff

    //region Keyboard stuff

    public static void dismissKeyboard(View view)
    {
        if (anyContext == null) return;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(View view)
    {
        if (anyContext == null) return;

        final View runview = view;

        appHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(runview, 0);
            }
        }, 500);
    }

    //endregion Keyboard stuff

    //region Application stuff

    @Nullable
    public static Drawable getIconFromAppStore(String apkname)
    {
        return CacheManager.getAppIcon(apkname);
    }

    @Nullable
    public static Drawable getIconFromApplication(String apkname)
    {
        return VersionUtils.getIconFromApplication(apkname);
    }

    public static boolean isAppInstalled(String packageName)
    {
        if (anyContext == null) return false;

        try
        {
            ApplicationInfo appInfo = anyContext.getPackageManager().getApplicationInfo(packageName, 0);

            return (appInfo != null);
        }
        catch (PackageManager.NameNotFoundException ignore)
        {
        }

        return false;
    }

    public static void installAppFromPlaystore(String packagename)
    {
        Intent goToMarket = new Intent(Intent.ACTION_VIEW);
        goToMarket.setData(Uri.parse("market://details?id=" + packagename));

        ProcessManager.launchIntent(goToMarket);
    }

    public static void uninstallApp(String packagename)
    {
        try
        {
            Uri packageUri = Uri.parse("package:" + packagename);
            Intent unInstall = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
            ProcessManager.launchIntent(unInstall);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    public static void launchApp(String packagename)
    {
        ProcessManager.launchApp(packagename);
    }

    //endregion Application stuff

    //region All purpose simple methods

    public static int parseNumber(String restag)
    {
        try
        {
            return Integer.parseInt(restag);
        }
        catch (Exception ignore)
        {
        }

        return 0;
    }

    public static boolean equals(String str1, String str2)
    {
        return (str1 == null) && (str2 == null) || (str1 != null) && (str2 != null) && str1.equals(str2);
    }

    public static boolean equalsIgnoreCase(String str1, String str2)
    {
        return (str1 == null) && (str2 == null) || (str1 != null) && (str2 != null) && str1.equalsIgnoreCase(str2);
    }

    public static boolean startsWith(String str1, String str2)
    {
        return (str1 != null) && (str2 != null) && str1.startsWith(str2);
    }

    public static int compareTo(String str1, String str2)
    {
        if ((str1 != null) && (str2 != null)) return str1.compareTo(str2);

        return 0;
    }

    public static void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);

        }
        catch (InterruptedException ignore)
        {
        }
    }

    public static long dezify(long number)
    {
        return number ^ 0x2905196228051998L;
    }

    @Nullable
    public static String dezify(String string)
    {
        if (string == null) return null;
        byte[] bytes = dezify(string.getBytes());
        if (bytes == null) return null;
        return new String(bytes);
    }

    @Nullable
    public static UUID dezify(UUID uuid)
    {
        if (uuid == null) return null;
        byte[] bytes = Simple.getUUIDBytes(uuid.toString());
        return UUID.fromString(Simple.getUUIDString(dezify(bytes)));
    }

    @Nullable
    public static byte[] dezify(byte[] bytes)
    {
        if (bytes == null) return null;

        byte[] dezi = {0x29, 0x05, 0x19, 0x62};

        for (int inx = 0; inx < bytes.length; inx++)
        {
            bytes[ inx ] = (byte) (bytes[ inx ] ^ (0x0f & dezi[ inx % 4 ]));
        }

        return bytes;
    }

    public static View removeFromParent(View view)
    {
        ((ViewGroup) view.getParent()).removeView(view);

        return view;
    }

    public static void makeRoundedCorners(View view, boolean solid)
    {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(4);

        if (solid)
        {
            shape.setColor(CommonConfigs.PreferenceTextButtonColor);
        }
        else
        {
            shape.setStroke(2, CommonConfigs.PreferenceTextButtonColor);
        }

        view.setPadding(12, 10, 10, 12);
        view.setBackground(shape);
    }

    public static void makeStandardButton(View view, boolean preferred)
    {
        makeRoundedCorners(view, preferred);

        view.setLayoutParams(Simple.layoutParamsMM(10, 0, 10, 0));

        if (view instanceof TextView)
        {
            ((TextView) view).setTextColor(preferred ? Color.WHITE : Color.BLACK);
            ((TextView) view).setTextSize(Simple.getPreferredTextSize());
            ((TextView) view).setAllCaps(true);
        }
    }

    public static void makeAlert(String text)
    {
        makeAlert(text, null);
    }

    public static void makeAlert(String text , String title)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(Simple.getActContext());

        builder.setTitle(title);
        builder.setMessage(text);
        builder.setPositiveButton("Ok", null);

        AlertDialog dialog = builder.create();
        dialog.show();
        Simple.adjustAlertDialog(dialog);
    }

    public static void makeToast(String text)
    {
        final String cbtext = text;

        makePost(new Runnable()
        {
            @Override
            public void run()
            {
                Toast toast = Toast.makeText(anyContext, cbtext, Toast.LENGTH_LONG);
                TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
                if (view != null) view.setGravity(Gravity.CENTER);
                toast.show();
            }
        });
    }

    public static void makePost(Runnable runnable)
    {
        if (appHandler != null) appHandler.post(runnable);
    }

    public static void makePost(Runnable runnable, int delay)
    {
        if (appHandler != null) appHandler.postDelayed(runnable, delay);
    }

    public static void removePost(Runnable runnable)
    {
        if (appHandler != null) appHandler.removeCallbacks(runnable);
    }

    public static void dumpDirectory(String path)
    {
        dumpDirectory(new File(path));
    }

    public static void dumpDirectory(File path)
    {
        File[] files = path.listFiles();

        Log.d(LOGTAG, "dumpDirectory: " + path.getAbsoluteFile());

        for (File file : files)
        {
            Log.d(LOGTAG, "dumpDirectory: => " + file.getAbsoluteFile());
        }
    }

    @Nullable
    public static byte[] readBinaryFile(File filename)
    {
        try
        {
            FileInputStream inputStream = new FileInputStream(filename);
            int size = (int) inputStream.getChannel().size();
            byte[] content = new byte[ size ];
            int xfer = inputStream.read(content);
            inputStream.close();

            return (xfer == size) ? content : null;
        }
        catch (FileNotFoundException ignore)
        {
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    public static boolean writeBinaryFile(File filename, byte[] content)
    {
        if (content != null)
        {
            try
            {
                FileOutputStream outputStream = new FileOutputStream(filename);
                outputStream.write(content);
                outputStream.close();

                return true;
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        return false;
    }

    @Nullable
    public static String readDatadirFile(String filename)
    {
        try
        {
            FileInputStream inputStream;
            inputStream = anyContext.openFileInput(filename);
            int size = (int) inputStream.getChannel().size();
            byte[] content = new byte[ size ];
            int xfer = inputStream.read(content);
            inputStream.close();

            return new String(content, 0, xfer);
        }
        catch (FileNotFoundException ignore)
        {
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    public static boolean writeDatadirFile(String filename, String content)
    {
        if (content != null)
        {
            try
            {
                FileOutputStream outputStream;
                outputStream = anyContext.openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(content.getBytes());
                outputStream.close();

                return true;
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        return false;
    }

    //endregion All purpose simple methods

    //region Haptic feedback

    public static void makeClick()
    {
        if (appContext != null)
        {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.playSoundEffect(SoundEffectConstants.CLICK);
        }
    }

    public static void makeVibration()
    {
        if (anyContext != null)
        {
            long pattern[] = {0, 200, 100, 300, 400};

            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(pattern, -1);
        }
    }

    public static void raiseSpeechVolume(int percent)
    {
        if (anyContext != null)
        {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            int maxvol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int curvol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            int dstvol = (maxvol * percent) / 100;
            if (dstvol > maxvol) dstvol = maxvol;

            if (curvol < dstvol) am.setStreamVolume(AudioManager.STREAM_MUSIC, dstvol, 0);
            curvol = am.getStreamVolume(AudioManager.STREAM_MUSIC);

            Log.d(LOGTAG, "raiseSpeechVolume: percent=" + percent + " " + dstvol + "=>" + curvol);
        }
    }

    public static void setSpeechVolume(int index)
    {
        if (anyContext != null)
        {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            am.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        }
    }

    public static int getSpeechVolume()
    {
        if (anyContext != null)
        {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            return am.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        return 0;
    }

    //endregion Haptic feedback

    //region All purpose simple getters

    public static boolean hasNavigationBar()
    {
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

        return !(hasBackKey && hasHomeKey);
    }

    public static int getDensityDPI()
    {
        return anyContext.getResources().getDisplayMetrics().densityDpi;
    }

    public static float getScaledDensity()
    {
        return anyContext.getResources().getDisplayMetrics().scaledDensity;
    }

    public static float getDensity()
    {
        return anyContext.getResources().getDisplayMetrics().density;
    }

    public static int getDevicePixels(int pixels)
    {
        return (pixels * getDensityDPI()) / 160;
    }

    public static float getDeviceTextSize(float textsize)
    {
        return textsize / getDensity();
    }

    public static void setPadding(View view, int left, int top, int right, int bottom)
    {
        view.setPadding(getDevicePixels(left),
                getDevicePixels(top),
                getDevicePixels(right),
                getDevicePixels(bottom));
    }

    public static void adjustAlertDialog(AlertDialog dialog)
    {
        View titleView = dialog.findViewById(getIdentifier("alertTitle", "id", "android"));

        if ((titleView != null) && (titleView instanceof TextView))
        {
            ((TextView) titleView).setTextSize(getPreferredTitleSize());
        }

        Button button;

        float textSize = Simple.getPreferredTextSize();

        button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (button != null) button.setTextSize(textSize);

        button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (button != null) button.setTextSize(textSize);

        button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        if (button != null) button.setTextSize(textSize);
    }

    @Nullable
    public static String getMatch(String regex, String content)
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) return null;

        return matcher.group(1);
    }

    public static String getCurrencySymbol()
    {
        Currency currency = Currency.getInstance(Locale.getDefault());
        return currency.getSymbol();
    }

    public static String getTrans(int resid, Object... args)
    {
        //
        // Check for You can say You to me.
        //

        Resources res = anyContext.getResources();

        int residdu = 0;

        if (Simple.equals(Simple.getSharedPrefString("owner.siezen"), "duzen"))
        {
            String resname = res.getResourceEntryName(resid) + "_du";
            residdu = res.getIdentifier(resname, "string", anyContext.getPackageName());
        }

        String message = res.getString(residdu > 0 ? residdu : resid);

        return String.format(message, args);
    }

    public static int getResArrayIdentifier(String resname)
    {
        Resources res = anyContext.getResources();
        return res.getIdentifier(resname, "array", anyContext.getPackageName());
    }

    public static String[] getTransArray(int resid)
    {
        return appContext.getResources().getStringArray(resid);
    }

    public static List<String> getTransList(int resid)
    {
        String[] array = getTransArray(resid);
        List<String> list = new ArrayList<>();

        Collections.addAll(list, array);

        return list;
    }

    @Nullable
    public static Map<String, String> getTransMap(int residkeys)
    {
        Resources res = appContext.getResources();

        String resname = res.getResourceEntryName(residkeys);
        if (!resname.endsWith("_keys")) return null;

        resname = resname.substring(0, resname.length() - 4) + "vals";
        int residvals = res.getIdentifier(resname, "array", anyContext.getPackageName());

        String[] keys = res.getStringArray(residkeys);
        String[] vals = res.getStringArray(residvals);

        Map<String, String> map = new LinkedHashMap<>();

        for (int inx = 0; inx < Math.min(keys.length, vals.length); inx++)
        {
            map.put(keys[ inx ], vals[ inx ]);
        }

        return map;
    }

    public static String getTransVal(int residkeys, String keyval)
    {
        Resources res = anyContext.getResources();

        String resname = res.getResourceEntryName(residkeys);
        if (!resname.endsWith("_keys")) return keyval;

        resname = resname.substring(0, resname.length() - 4) + "vals";
        int residvals = res.getIdentifier(resname, "array", anyContext.getPackageName());

        String[] keys = res.getStringArray(residkeys);
        String[] vals = res.getStringArray(residvals);

        for (int inx = 0; (inx < keys.length) && (inx < vals.length); inx++)
        {
            if (keys[ inx ].equals(keyval)) return vals[ inx ];
        }

        return keyval;
    }

    public static boolean fileCopy(File src, File dst)
    {
        boolean result = false;

        InputStream in = null;
        OutputStream out = null;

        try
        {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);

            byte[] buf = new byte[ 8192 ];
            int len;

            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }

            result = true;
        }
        catch (IOException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        if (in != null)
        {
            try
            {
                in.close();
            }
            catch (IOException ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        try
        {
            if (out != null) out.close();
        }
        catch (IOException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return result;
    }

    private static final Map<String, File> externalDirs = new HashMap<>();

    public static File getMediaDirType(String dirtype)
    {
        if (externalDirs.containsKey(dirtype)) return externalDirs.get(dirtype);
        File extdir = Environment.getExternalStoragePublicDirectory(dirtype);
        externalDirs.put(dirtype, extdir);

        return extdir;
    }

    public static void makeDirectory(File path)
    {
        if (! path.exists())
        {
            if (! path.mkdirs())
            {
                OopsService.log(LOGTAG, "Cannot create directory: " + path.toString());
            }
        }
    }

    public static void makeFullpath(String path)
    {
        makeFullpath(new File(path));
    }

    public static void makeFullpath(File path)
    {
        if (! path.getParentFile().exists())
        {
            if (! path.getParentFile().mkdirs())
            {
                OopsService.log(LOGTAG, "Cannot create directory: " + path.getParentFile());
            }
        }
    }

    public static void makeDirectory(String path)
    {
        makeDirectory(new File(path));
    }

    public static File getMediaPath(String disposition)
    {
        if (disposition.equals("recordings"))
        {
            File dir = getMediaDirType(Environment.DIRECTORY_MOVIES);
            return new File(dir, "Recordings");
        }

        if (disposition.equals("screenshots"))
        {
            File dir = getMediaDirType(Environment.DIRECTORY_DCIM);
            return new File(dir, "Screenshots");
        }

        if (disposition.equals("camera"))
        {
            File dir = getMediaDirType(Environment.DIRECTORY_DCIM);
            return new File(dir, "Camera");
        }

        if (disposition.equals("family"))
        {
            File dir = getMediaDirType(Environment.DIRECTORY_DCIM);
            return new File(dir, "Family");
        }

        if (disposition.equals("incoming"))
        {
            File dir = getMediaDirType(Environment.DIRECTORY_DCIM);
            return new File(dir, "Incoming");
        }

        if (disposition.equals("download"))
        {
            return getMediaDirType(Environment.DIRECTORY_DOWNLOADS);
        }

        if (disposition.equals("whatsapp"))
        {
            File dir = getExternalStorageDir();
            return new File(dir, "WhatsApp/Media/WhatsApp Images");
        }

        if (disposition.equals("profiles"))
        {
            File dir = getExternalFilesDir();
            File profiles = new File(dir, "profiles");

            if (! (profiles.exists() || ! profiles.mkdirs()))
            {
                Log.d(LOGTAG, "getMediaPath: failed create pofile:" + profiles.toString());
            }

            return profiles;
        }

        if (disposition.equals("social"))
        {
            File dir = getExternalFilesDir();
            File social = new File(dir, "social");

            if (! (social.exists() || ! social.mkdirs()))
            {
                Log.d(LOGTAG, "getMediaPath: failed create pofile:" + social.toString());
            }

            return social;
        }

        if (disposition.equals("misc"))
        {
            File dir = getMediaDirType(Environment.DIRECTORY_DCIM);
            return new File(dir, "Miscellanous");
        }

        File dir = getMediaDirType(Environment.DIRECTORY_DCIM);
        return new File(dir, "Miscellanous");
    }

    public static void removeFiles(File dir, String suffix)
    {
        File[] files = dir.listFiles();

        for (File file : files)
        {
            if (file.toString().endsWith(suffix))
            {
                if (file.delete()) Log.d(LOGTAG, "removeFiles: " + file.toString());
            }
        }
    }

    public static File getFilesDir()
    {
        return getAnyContext().getFilesDir();
    }

    private static File externalFilesDir;
    private static File externalCacheDir;

    public static File getExternalFilesDir()
    {
        if (externalFilesDir != null) return externalFilesDir;
        externalFilesDir = getAnyContext().getExternalFilesDir(null);
        return externalFilesDir;
    }

    public static File getExternalCacheDir()
    {
        if (externalCacheDir != null) return externalCacheDir;
        externalCacheDir = getAnyContext().getExternalCacheDir();
        return externalCacheDir;
    }

    public static File getCacheDir()
    {
        return getAnyContext().getCacheDir();
    }

    private static File externalStorageDir;

    public static File getExternalStorageDir()
    {
        //
        // Avoid extremly annoying system log message if
        // SD-Card is not installed.
        //

        if (externalStorageDir != null) return externalStorageDir;
        externalStorageDir = Environment.getExternalStorageDirectory();
        return externalStorageDir;
    }

    public static File getPackageFile(String name)
    {
        return new File(Simple.getFilesDir(), Simple.getPackageName() + "." + name);
    }

    public static File getIdentityFile(String name)
    {
        File identdir =  new File(Simple.getExternalFilesDir(), SystemIdentity.getIdentity());

        if ((! identdir.exists()) && ! identdir.mkdirs())
        {
            Log.d(LOGTAG, "Creating identity directory failed:" + identdir.toString());
        }

        return new File(identdir, name);
    }

    public static String getTempfile(String filename)
    {
        return new File(Simple.getAnyContext().getCacheDir(), filename).toString();
    }

    public static long getFilesize(String filepath)
    {
        if (filepath == null) return 0;
        File file = new File(filepath);
        if (!file.exists()) return 0;

        return file.length();
    }

    public static String getFileNameOnly(String clobfile)
    {
        if (clobfile.lastIndexOf('.') >= 0)
        {
            return clobfile.substring(0, clobfile.lastIndexOf('.'));
        }

        return clobfile;
    }

    public static String getFileExtensionOnly(String clobfile)
    {
        if (clobfile.lastIndexOf('.') >= 0)
        {
            return clobfile.substring(clobfile.lastIndexOf('.'), clobfile.length());
        }

        return "";
    }

    public static String getFilename(String filepath)
    {
        return new File(filepath).getName();
    }

    public static String getDirectory(String filepath)
    {
        return new File(filepath).getParent();
    }

    public static int getRandom(int min, int max)
    {
        return new Random().nextInt(max - min) + min;
    }

    public static String getUUID()
    {
        return UUID.randomUUID().toString();
    }

    public static NotificationManager getNotificationManager()
    {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static InputMethodManager getInputMethodManager()
    {
        return (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public static String getAppName()
    {
        return anyContext.getString(anyContext.getApplicationInfo().labelRes);
    }

    public static String getOwnerName()
    {
        String prefix = getSharedPrefString("owner.prefix");
        String first = getSharedPrefString("owner.firstname");
        String given = getSharedPrefString("owner.givenname");

        if ((first != null) && (given != null)) return first + " " + given;
        if ((prefix != null) && (given != null)) return prefix + " " + given;

        return (given != null) ? given : "Unbekannt";
    }

    public static String getPackageName()
    {
        return anyContext.getPackageName();
    }

    public static String getDefaultEmail()
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("mailto:"));
        ResolveInfo res = anyContext.getPackageManager().resolveActivity(intent, 0);

        return (res.activityInfo == null) ? null : res.activityInfo.packageName;
    }

    @Nullable
    public static String getDefaultGatewayAddress()
    {
        if (anyContext == null) return null;

        try
        {
            if (wifiManager == null)
            {
                wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            }

            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

            int gw = dhcpInfo.gateway;

            if (gw != 0)
            {
                return (gw & 0xff) + "." + ((gw >> 8) & 0xff) + "." +
                        ((gw >> 16) & 0xff) + "." + ((gw >> 24) & 0xff);
            }
        }
        catch (Exception ignore)
        {
        }

        return null;
    }

    @Nullable
    public static String getPhoneNumber()
    {
        try
        {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            return tm.getLine1Number();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static String getWifiIPAddress()
    {
        if (anyContext == null) return null;

        try
        {
            if (wifiManager == null)
            {
                wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            }

            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

            int ip = dhcpInfo.ipAddress;

            if (ip != 0)
            {
                return (ip & 0xff) + "." + ((ip >> 8) & 0xff) + "." +
                        ((ip >> 16) & 0xff) + "." + ((ip >> 24) & 0xff);
            }
        }
        catch (Exception ignore)
        {
        }

        return null;
    }

    @Nullable
    public static String getWifiName()
    {
        if (anyContext == null) return null;

        try
        {
            if (wifiManager == null)
            {
                wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            }

            return wifiManager.getConnectionInfo().getSSID().replace("\"", "");
        }
        catch (Exception ignore)
        {
        }

        return null;
    }

    @Nullable
    public static ConnectivityManager getConnectivityManager()
    {
        if (anyContext != null)
        {
            return (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        return null;
    }

    public static boolean isWifiConnected()
    {
        ConnectivityManager cm = getConnectivityManager();

        if (cm != null)
        {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (activeNetwork != null)
            {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) return true;
            }
        }

        return false;
    }

    public static boolean isMobileConnected()
    {
        ConnectivityManager cm = getConnectivityManager();

        if (cm != null)
        {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (activeNetwork != null)
            {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) return true;
            }
        }

        return false;
    }

    public static boolean isSameSubnet(String ip1, String ip2)
    {
        if ((ip1 == null) || (ip2 == null)) return false;

        String[] ip1parts = ip1.split("\\.");
        String[] ip2parts = ip2.split("\\.");

        //noinspection SimplifiableIfStatement
        if (ip1parts.length != ip2parts.length) return false;

        return ip1parts[ 0 ].equals(ip2parts[ 0 ]) &&
                ip1parts[ 1 ].equals(ip2parts[ 1 ]) &&
                ip1parts[ 2 ].equals(ip2parts[ 2 ]);
    }

    @Nullable
    public static String getMacAddress()
    {
        if (anyContext == null) return null;

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

    public static int getDeviceWidth()
    {
        if (anyContext == null) return 0;

        DisplayMetrics displayMetrics = anyContext.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static int getDeviceHeight()
    {
        if (anyContext == null) return 0;

        DisplayMetrics displayMetrics = anyContext.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    public static float getPreferredEditSize()
    {
        return getDeviceTextSize(24f);
    }

    public static float getPreferredTextSize()
    {
        return getDeviceTextSize(22f);
    }

    public static float getPreferredTitleSize()
    {
        return getDeviceTextSize(24f);
    }

    public static void setFontScale()
    {
        if (getResources().getConfiguration().fontScale > 1.0f)
        {
            Settings.System.putFloat(getContentResolver(), Settings.System.FONT_SCALE, 1.0f);

            Configuration configuration = getResources().getConfiguration();
            configuration.fontScale = 1.0f;

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            metrics.scaledDensity = configuration.fontScale * metrics.density;

            getResources().updateConfiguration(configuration, metrics);

            Log.d(LOGTAG, "setFontScale: adjusted.");
        }
    }

    public static boolean isTablet()
    {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static int getActionBarHeight()
    {
        TypedValue tv = new TypedValue();

        if (anyContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            DisplayMetrics dm = anyContext.getResources().getDisplayMetrics();
            return TypedValue.complexToDimensionPixelSize(tv.data, dm);
        }

        return 64;
    }

    public static int getStatusBarHeight()
    {
        int resourceId = anyContext.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) return anyContext.getResources().getDimensionPixelSize(resourceId);

        return 20;
    }

    public static int getNavigationBarHeight()
    {
        int resourceId = anyContext.getResources()
                .getIdentifier("navigation_bar_height", "dimen", "android");

        if (resourceId > 0) return anyContext.getResources().getDimensionPixelSize(resourceId);

        return 64;
    }

    @SuppressWarnings("SameReturnValue")
    public static String getDeviceName()
    {
        return android.os.Build.MODEL;
    }

    public static byte[] getUUIDBytes(String uuid)
    {
        String uuidstr = uuid.replace("-", "");
        return getHexStringToBytes(uuidstr);
    }

    public static String getUUIDString(byte[] bytes)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }

    public static String getHexBytesToString(byte[] bytes)
    {
        if (bytes == null) return null;

        return getHexBytesToString(bytes, 0, bytes.length);
    }

    public static String getHexBytesToString(byte[] bytes, int offset, int length)
    {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[ length << 1 ];

        for (int inx = offset; inx < (length + offset); inx++)
        {
            //noinspection PointlessArithmeticExpression
            hexChars[ ((inx - offset) << 1) + 0 ] = hexArray[ (bytes[ inx ] >> 4) & 0x0f ];
            //noinspection PointlessBitwiseExpression
            hexChars[ ((inx - offset) << 1) + 1 ] = hexArray[ (bytes[ inx ] >> 0) & 0x0f ];
        }

        return String.valueOf(hexChars);
    }

    public static byte[] getHexStringToBytes(String hexstring)
    {
        if (hexstring == null) return null;

        byte[] bytes = new byte[ hexstring.length() >> 1 ];

        for (int inx = 0; inx < hexstring.length(); inx += 2)
        {
            //noinspection PointlessBitwiseExpression,PointlessArithmeticExpression
            bytes[ inx >> 1 ] = (byte)
                    ((Character.digit(hexstring.charAt(inx + 0), 16) << 4)
                            + Character.digit(hexstring.charAt(inx + 1), 16) << 0);
        }

        return bytes;
    }

    public static byte[] appendBytes(byte[] buffer, byte[] append)
    {
        if (append == null) return buffer;

        return appendBytes(buffer, append, 0, append.length);
    }

    public static byte[] appendBytes(byte[] buffer, byte[] append, int offset, int size)
    {
        if (append == null) return buffer;
        if (buffer == null) return null;

        byte[] newbuf = new byte[ buffer.length + size ];

        System.arraycopy(buffer, 0, newbuf, 0, buffer.length);
        System.arraycopy(append, offset, newbuf, buffer.length, size);

        return newbuf;
    }

    @Nullable
    public static byte[] getAllInputData(InputStream input)
    {
        byte[] buffer = new byte[ 0 ];
        byte[] chunk = new byte[ 8192 ];
        int xfer;

        try
        {
            while ((xfer = input.read(chunk)) > 0)
            {
                buffer = appendBytes(buffer, chunk, 0, xfer);
            }
        }
        catch (IOException ex)
        {
            OopsService.log(LOGTAG, ex);

            return null;
        }

        return buffer;
    }

    public static String getAllInput(InputStream input)
    {
        StringBuilder string = new StringBuilder();
        byte[] buffer = new byte[ 4096 ];
        int xfer;

        try
        {
            while ((xfer = input.read(buffer)) > 0)
            {
                string.append(new String(buffer, 0, xfer));
            }
        }
        catch (IOException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return string.toString();
    }

    @Nullable
    public static byte[] getFileBytes(File file)
    {
        try
        {
            if (file.exists())
            {
                InputStream in = new FileInputStream(file);
                int len = (int) file.length();
                byte[] bytes = new byte[ len ];

                int xfer = 0;
                while (xfer < len) xfer += in.read(bytes, xfer, len - xfer);
                in.close();

                return bytes;
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static InputStream getInputStream(String contentUrl)
    {
        try
        {
            Uri contentUri = Uri.parse(contentUrl);
            return anyContext.getContentResolver().openInputStream(contentUri);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static String getFileContent(File file)
    {
        byte[] bytes = getFileBytes(file);
        return (bytes == null) ? null : new String(bytes);
    }

    public static boolean putFileBytes(File file, byte[] bytes)
    {
        if (bytes == null) return false;

        try
        {
            OutputStream out = new FileOutputStream(file);
            out.write(bytes);
            out.close();

            return true;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return false;
    }

    public static boolean putFileContent(File file, String content)
    {
        return putFileBytes(file, content.getBytes());
    }

    public static File changeExtension(File file, String extension)
    {
        return new File(file.getParent(), file.getName().replaceAll("\\.[a-zA-Z0-9]*$", extension));
    }

    public static String changeExtension(String file, String extension)
    {
        return file.replaceAll("\\.[a-zA-Z0-9]*$", extension);
    }

    @Nullable
    public static Drawable getDrawableSquare(String file, int size)
    {
        Bitmap bm = getBitmapThumbnail(file, size);
        if (bm == null) return null;

        int ssize = (bm.getHeight() <= bm.getWidth()) ? bm.getHeight() : bm.getWidth();
        int offsx = (bm.getWidth() - ssize) / 2;
        int offsy = (bm.getHeight() - ssize) / 2;

        Bitmap fb = Bitmap.createBitmap(bm, offsx, offsy, ssize, ssize);
        return new BitmapDrawable(appContext.getResources(), fb);
    }

    @Nullable
    public static Drawable getDrawableThumbnail(String file, int size)
    {
        Bitmap myBitmap = getBitmapThumbnail(file, size);
        if (myBitmap == null) return null;

        return new BitmapDrawable(appContext.getResources(), myBitmap);
    }

    @Nullable
    public static Drawable getDrawable(Bitmap bitmap)
    {
        return (bitmap == null) ? null : new BitmapDrawable(appContext.getResources(), bitmap);
    }

    @Nullable
    public static Bitmap getBitmapThumbnail(String file, int size)
    {
        try
        {
            BitmapFactory.Options orgsize = new BitmapFactory.Options();
            orgsize.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file, orgsize);

            int wid = orgsize.outWidth;
            int hei = orgsize.outHeight;

            int scale = 1;

            while ((wid > size) && (hei > size))
            {
                wid = wid >> 1;
                hei = hei >> 1;
                scale = scale << 1;
            }

            BitmapFactory.Options scalesize = new BitmapFactory.Options();
            scalesize.inSampleSize = scale;
            return BitmapFactory.decodeFile(file, scalesize);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static Drawable getDrawable(int iconres)
    {
        return VersionUtils.getDrawableFromResources(anyContext, iconres);
    }

    @Nullable
    public static Drawable getDrawableFromFile(String file)
    {
        Bitmap myBitmap = getBitmap(file);
        if (myBitmap == null) return null;

        return new BitmapDrawable(appContext.getResources(), myBitmap);
    }

    @Nullable
    public static Bitmap getBitmap(String file)
    {
        try
        {
            return BitmapFactory.decodeFile(file);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static Bitmap getBitmap(File file)
    {
        try
        {
            return BitmapFactory.decodeFile(file.toString());
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    public static Bitmap getBitmap(int resid)
    {
        return BitmapFactory.decodeResource(anyContext.getResources(), resid);
    }

    public static int getIconResourceId(String iconname)
    {
        return anyContext.getResources().getIdentifier(
                iconname, "drawable", anyContext.getPackageName());
    }

    @Nullable
    public static String getFirstMatch(String regex, String content)
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) return null;

        return matcher.group(1);
    }

    public static byte[] getCRC16ccittCompute(byte[] byteArray, int length)
    {
        int crc = 0xffff;

        byte byt;
        boolean bitbyt;
        boolean bitcrc;

        for (int inx = 0; inx < length; inx++)
        {
            byt = byteArray[ inx ];

            for (int cnt = 0; cnt < 8; cnt++)
            {
                bitbyt = (((byt >> (7 - cnt)) & 1) == 1);
                bitcrc = (((crc >> 15) & 1) == 1);

                crc <<= 1;

                if (bitcrc ^ bitbyt) crc ^= 4129;
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(crc & 0xffff);

        return new byte[]{buffer.array()[ 0 ], buffer.array()[ 1 ]};
    }

    public static byte[] getCRC16ccittEmbed(byte[] byteArray)
    {
        byte[] cc = getCRC16ccittCompute(byteArray, byteArray.length - 2);

        byteArray[ byteArray.length - 2 ] = cc[ 0 ];
        byteArray[ byteArray.length - 1 ] = cc[ 1 ];

        return byteArray;
    }

    public static boolean getCRC16ccittVerify(byte[] bytes)
    {
        byte[] newCrc = getCRC16ccittCompute(bytes, bytes.length - 2);

        return !((newCrc[ 0 ] != bytes[ bytes.length - 2 ]) ||
                (newCrc[ 1 ] != bytes[ bytes.length - 1 ]));
    }

    public static int getIntFromLEByteArray(byte[] data)
    {
        return getIntFromByteArray(data, true);
    }

    public static int getIntFromBEByteArray(byte[] data)
    {
        return getIntFromByteArray(data, false);
    }

    public static int getIntFromByteArray(byte[] data, boolean littleEndian)
    {
        int returnValue = 0;

        int start = littleEndian ? data.length - 1 : 0;

        for (int offset = 0; offset < data.length; offset++)
        {
            int index = Math.abs(start - offset);

            returnValue |= (data[ index ] & 0xff) << (index << 3);
        }

        return returnValue;
    }

    public static byte[] getReversedBytes(byte[] array)
    {
        int len = array.length;

        byte[] reversedArray = new byte[ len ];

        for (int index = 0; index < len; index++)
        {
            reversedArray[ (len - index) - 1 ] = array[ index ];
        }

        return reversedArray;
    }

    //endregion All purpose simple getters

    //region JSON stuff

    @Nullable
    public static JSONObject JSONClone(JSONObject json)
    {
        try
        {
            return new JSONObject(json.toString());
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    public static void JSONremove(JSONObject json, String key)
    {
        json.remove(key);
    }

    public static void JSONput(JSONObject json, String key, Object val)
    {
        try
        {
            json.put(key, val);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    @Nullable
    public static String JSONgetString(JSONObject json, String key)
    {
        try
        {
            return json.getString(key);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static JSONArray JSONgetArray(JSONObject json, String key)
    {
        try
        {
            return json.getJSONArray(key);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static JSONObject JSONgetObject(JSONObject json, String key)
    {
        try
        {
            return json.getJSONObject(key);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static JSONObject JSONgetObject(JSONArray json, int index)
    {
        try
        {
            return json.getJSONObject(index);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static String JSON2String(JSONObject jsonObject)
    {
        if (jsonObject != null)
        {
            try
            {
                return jsonObject.toString(2);
            }
            catch (JSONException ignored)
            {
            }
        }

        return null;
    }

    @Nullable
    public static String UTF8defuck(String utf8)
    {
        //
        // Remove
        //
        // LEFT-TO-RIGHT EMBEDDING
        // POP DIRECTIONAL FORMATTING
        //
        // fuck characters.
        //

        if (utf8 != null)
        {
            utf8 = utf8.replace("\u202A", "");
            utf8 = utf8.replace("\u202C", "");
        }

        return utf8;
    }

    public static String JSONdefuck(String json)
    {
        //
        // I hate slash escaping.
        //

        return json.replace("\\/", "/");
    }

    //endregion JSON stuff

    //region Date and time

    private static final String ISO8601DATENOSECS = "yyyy-MM-dd'T'HH:mm'Z'";
    private static final String ISO8601DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ISO8601DATEFORMATMS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static long nowAsTimeStamp()
    {
        return new Date().getTime();
    }

    public static String nowAsISO()
    {
        return timeStampAsISO(nowAsTimeStamp());
    }

    public static long todayAsTimeStamp()
    {
        Calendar calendar = new GregorianCalendar();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        calendar = new GregorianCalendar(year, month, day);

        return calendar.getTimeInMillis();
    }

    public static String todayAsISO()
    {
        return timeStampAsISO(todayAsTimeStamp());
    }

    public static String todayAsISO(int plusdays)
    {
        return timeStampAsISO(todayAsTimeStamp() + (86400L * 1000L * plusdays));
    }

    public static String timeStampAsISO(long timestamp)
    {
        DateFormat df = new SimpleDateFormat(ISO8601DATEFORMAT, Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date(timestamp));
    }

    public static String timeStampUTCAsLocalISO(long timestamp)
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String isotime = df.format(new Date(timestamp));
        isotime += TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);

        return isotime;
    }

    public static long getTimeStamp(String isodate)
    {
        if (isodate == null) return 0;

        try
        {
            SimpleDateFormat df = new SimpleDateFormat(ISO8601DATEFORMAT, Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.parse(isodate).getTime();
        }
        catch (ParseException ignore)
        {
            //
            // Date missing seconds?
            //
        }

        try
        {
            SimpleDateFormat df = new SimpleDateFormat(ISO8601DATENOSECS, Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.parse(isodate).getTime();
        }
        catch (ParseException ex)
        {
            //
            // Date missing seconds?
            //
        }

        try
        {
            SimpleDateFormat df = new SimpleDateFormat(ISO8601DATEFORMATMS, Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.parse(isodate).getTime();
        }
        catch (ParseException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return 0;
    }

    public static String getLocalDayOfMonth(long timeStamp)
    {
        DateFormat df = new SimpleDateFormat("d", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        return df.format(timeStamp);
    }

    public static String getLocalDayOfWeek(long timeStamp)
    {
        DateFormat df = new SimpleDateFormat("EEEE", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        return df.format(timeStamp);
    }

    public static String getLocalMonth(long timeStamp)
    {
        DateFormat df = new SimpleDateFormat("MMMM", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        return df.format(timeStamp);
    }

    public static String get24HHour(String isodate)
    {
        return get24HHour(getTimeStamp(isodate));
    }

    public static String get24HHour(long timeStamp)
    {
        DateFormat df = new SimpleDateFormat("HH", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date(timeStamp));
    }

    public static String getLocal24HTime(long timeStamp)
    {
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        return df.format(timeStamp);
    }

    public static String getLocal24HTime(String isodate)
    {
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        return df.format(new Date(getTimeStamp(isodate)));
    }

    public static String getLocalDate(long timeStamp)
    {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        return df.format(new Date(timeStamp));
    }

    public static String getLocalDate(String isodate)
    {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        return df.format(new Date(getTimeStamp(isodate)));
    }

    public static String getLocalDateLong(String isodate)
    {
        DateFormat df = new SimpleDateFormat("dd. MMMM yyyy", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        return df.format(new Date(getTimeStamp(isodate)));
    }

    public static int getSecondsAgo(String isodate)
    {
        long iso = getTimeStamp(isodate);
        long now = new Date().getTime();

        iso /= 1000;
        now /= 1000;

        return (int) (now - iso);
    }

    public static int getDaysAgo(String isodate)
    {
        long iso = getTimeStamp(isodate);
        long now = new Date().getTime();

        iso /= 86400 * 1000;
        now /= 86400 * 1000;

        return (int) (now - iso);
    }

    public static long getLocalTimeToUTC(long timestamp)
    {
        try
        {
            String isodate = timeStampAsISO(timestamp);
            SimpleDateFormat df = new SimpleDateFormat(ISO8601DATEFORMAT, Locale.getDefault());
            df.setTimeZone(TimeZone.getDefault());
            return df.parse(isodate).getTime();
        }
        catch (ParseException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return 0L;
    }

    //endregion Date and time

    //region Layout params

    public static final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    public static LinearLayout.LayoutParams layoutParamsXX(int width, int height)
    {
        return new LinearLayout.LayoutParams(width, height);
    }

    public static LinearLayout.LayoutParams layoutParamsXX(int width, int height, int gravity)
    {
        return new LinearLayout.LayoutParams(width, height, gravity);
    }

    public static ViewGroup.LayoutParams layoutParamsWW()
    {
        return new ViewGroup.LayoutParams(WC, WC);
    }

    public static ViewGroup.LayoutParams layoutParamsMM()
    {
        return new ViewGroup.LayoutParams(MP, MP);
    }

    public static ViewGroup.LayoutParams layoutParamsMW()
    {
        return new ViewGroup.LayoutParams(MP, WC);
    }

    public static ViewGroup.LayoutParams layoutParamsWM()
    {
        return new ViewGroup.LayoutParams(WC, MP);
    }

    public static FrameLayout.LayoutParams layoutParamsWW(int gravity)
    {
        return new FrameLayout.LayoutParams(WC, WC, gravity);
    }

    public static FrameLayout.LayoutParams layoutParamsMM(int gravity)
    {
        return new FrameLayout.LayoutParams(MP, MP, gravity);
    }

    public static FrameLayout.LayoutParams layoutParamsMW(int gravity)
    {
        return new FrameLayout.LayoutParams(MP, WC, gravity);
    }

    public static FrameLayout.LayoutParams layoutParamsWM(int gravity)
    {
        return new FrameLayout.LayoutParams(WC, MP, gravity);
    }

    public static LinearLayout.LayoutParams layoutParamsWW(int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WC, WC);
        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    public static LinearLayout.LayoutParams layoutParamsMM(int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MP, MP);
        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    public static LinearLayout.LayoutParams layoutParamsMW(int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MP, WC);
        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    public static LinearLayout.LayoutParams layoutParamsWM(int left, int top, int right, int bottom)
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WC, MP);
        lp.setMargins(left, top, right, bottom);
        return lp;
    }

    //endregion Layout params

    //region Directory stuff

    public static JSONArray getDirectorySortedByAge(File dir, FilenameFilter ff, boolean desc)
    {
        if ((dir == null) || !dir.isDirectory()) return null;

        String[] dirlist = dir.list(ff);
        if (dirlist == null) return null;

        JSONArray list = new JSONArray();

        for (String name : dirlist)
        {
            File file = new File(dir, name);

            JSONObject entry = new JSONObject();

            Json.put(entry, "file", file.toString());
            Json.put(entry, "time", timeStampAsISO(file.lastModified()));

            Json.put(list, entry);
        }

        return Json.sort(list, "time", desc);
    }

    public static String[] getDirectoryAsArray(File dir, FilenameFilter filter)
    {
        String[] files = null;

        try
        {
            files = dir.list(filter);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return (files != null) ? files : new String[ 0 ];
    }

    public static ArrayList<String> getDirectoryAsList(File dir, FilenameFilter filter)
    {
        return new ArrayList<>(Arrays.asList(getDirectoryAsArray(dir, filter)));
    }

    public static class FileFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return new File(dir, name).isFile();
        }
    }

    public static class DirFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return new File(dir, name).isDirectory();
        }
    }

    public static boolean isImage(String name)
    {
        final String[] exts = new String[]{".jpg", ".png", ".gif", ".jpeg"};

        for (String ext : exts)
        {
            if (name.toLowerCase().endsWith(ext))
            {
                return true;
            }
        }

        return false;
    }

    public static class ImageFileFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return new File(dir, name).isFile() && isImage(name);
        }
    }

    public static boolean isVideo(String name)
    {
        final String[] exts = new String[]{".mp4", ".mpg"};

        for (String ext : exts)
        {
            if (name.toLowerCase().endsWith(ext))
            {
                return true;
            }
        }

        return false;
    }

    public static class VideoFileFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return new File(dir, name).isFile() && isVideo(name);
        }
    }

    //endregion Directory stuff

    //region Preference stuff

    public static boolean sharedPrefEquals(String key, String equals)
    {
        return equals(getSharedPrefs().getString(key, null), equals);
    }

    public static SharedPreferences getSharedPrefs()
    {
        return PreferenceManager.getDefaultSharedPreferences(anyContext);
    }

    public static int getSharedPrefInt(String key)
    {
        try
        {
            return getSharedPrefs().getInt(key, 0);
        }
        catch (Exception ex)
        {
            return 0;
        }
    }

    public static boolean getSharedPrefBoolean(String key)
    {
        try
        {
            return getSharedPrefs().getBoolean(key, false);
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    @Nullable
    public static String getSharedPrefString(String key)
    {
        try
        {
            return getSharedPrefs().getString(key, null);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    @Nullable
    public static Set<String> getSharedPrefStringSet(String key)
    {
        try
        {
            return getSharedPrefs().getStringSet(key, null);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static void setSharedPrefString(String key, String value)
    {
        getSharedPrefs().edit().putString(key, value).apply();
    }

    public static void setSharedPrefInt(String key, int value)
    {
        getSharedPrefs().edit().putInt(key, value).apply();
    }

    public static void setSharedPrefBoolean(String key, boolean value)
    {
        getSharedPrefs().edit().putBoolean(key, value).apply();
    }

    public static Map<String, Object> getAllPreferences(String prefix)
    {
        Map<String, Object> result = new HashMap<>();

        Map<String, ?> prefs = getSharedPrefs().getAll();

        for (Map.Entry<String, ?> entry : prefs.entrySet())
        {
            if ((prefix != null) && !entry.getKey().startsWith(prefix)) continue;

            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static boolean hasSharedPref(String key)
    {
        return Simple.getSharedPrefs().contains(key);
    }

    public static void removeSharedPref(String key)
    {
        if (Simple.getSharedPrefs().contains(key))
        {
            Log.w(LOGTAG, "removeSharedPref: " + key);
            Simple.getSharedPrefs().edit().remove(key).commit();
        }
    }

    public static void removeAllPreferences(String prefix)
    {
        SharedPreferences sp = getSharedPrefs();

        Map<String, ?> prefs = sp.getAll();

        for (Map.Entry<String, ?> entry : prefs.entrySet())
        {
            if (!entry.getKey().startsWith(prefix)) continue;

            sp.edit().remove(entry.getKey()).apply();
        }
    }

    //endregion Preference stuff

    public static String getLocale()
    {
        return Locale.getDefault().getLanguage() + "-r" + Locale.getDefault().getCountry();
    }

    public static String getLocaleCountry()
    {
        return Locale.getDefault().getCountry();
    }

    public static String getLocaleLanguage()
    {
        return Locale.getDefault().getLanguage();
    }

    //
    // Opens a HTTP connection and resolves illegal underscores in host names.
    //

    public static HttpURLConnection openUnderscoreConnection(String src) throws Exception
    {
        return openUnderscoreConnection(src, true);
    }

    public static HttpURLConnection openUnderscoreConnection(String src, boolean connect) throws Exception
    {
        URL url = new URL(src);

        String host = null;

        if (url.getHost().contains("_"))
        {
            host = url.getHost();

            InetAddress ip = InetAddress.getByName(host);
            src = src.replace(host, ip.getHostAddress());
            url = new URL(src);
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (host != null) connection.setRequestProperty("Host", host);

        connection.setUseCaches(false);
        connection.setDoInput(true);

        if (connect) connection.connect();

        return connection;
    }
}
