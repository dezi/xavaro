package de.xavaro.android.common;

import android.net.DhcpInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
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
import android.os.Handler;
import android.os.Environment;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.Uri;
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
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

    private static Activity appContext;
    private static Handler appHandler;
    private static Context anyContext;

    public static void setAppContext(Activity context)
    {
        Simple.appContext = context;
        Simple.anyContext = context;
        Simple.appHandler = new Handler();
    }

    public static Activity getAppContext()
    {
        return Simple.appContext;
    }

    public static void setAnyContext(Context context)
    {
        Simple.anyContext = context;
    }

    public static Context getAnyContext()
    {
        return Simple.anyContext;
    }

    public static Resources getResources()
    {
        return Simple.anyContext.getResources();
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

        InputMethodManager imm = (InputMethodManager) anyContext.getSystemService(Context.INPUT_METHOD_SERVICE);
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
                InputMethodManager imm = (InputMethodManager) anyContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(runview, 0);
            }
        }, 500);
    }

    //endregion Keyboard stuff

    //region Application stuff

    @Nullable
    public static Drawable getIconFromAppStore(String apkname)
    {
        return CacheManager.getIconFromAppStore(anyContext, apkname);
    }

    @Nullable
    public static Drawable getIconFromApplication(String apkname)
    {
        return VersionUtils.getIconFromApplication(anyContext, apkname);
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
        if (appContext == null) return;

        Intent goToMarket = new Intent(Intent.ACTION_VIEW);
        goToMarket.setData(Uri.parse("market://details?id=" + packagename));

        ProcessManager.launchIntent(appContext, goToMarket);
    }

    public static void uninstallApp(String packagename)
    {
        if (anyContext == null) return;

        try
        {
            Uri packageUri = Uri.parse("package:" + packagename);
            Intent unInstall = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
            anyContext.startActivity(unInstall);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    public static void launchApp(String packagename)
    {
        ProcessManager.launchApp(appContext, packagename);
    }

    public static void startActivityForResult(Intent intent, int tag)
    {
        appContext.startActivityForResult(intent, tag);
    }

    //endregion Application stuff

    //region All purpose simple methods

    public static boolean equals(String str1, String str2)
    {
        if ((str1 == null) && (str2 == null)) return true;
        return (str1 != null) && (str2 != null) && str1.equals(str2);
    }

    public static boolean equalsIgnoreCase(String str1, String str2)
    {
        if ((str1 == null) && (str2 == null)) return true;
        return (str1 != null) && (str2 != null) && str1.equalsIgnoreCase(str2);
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

    public static String dezify(String string)
    {
        byte[] dezi = {0x29, 0x05, 0x19, 0x62};
        byte[] bytes = string.getBytes();

        for (int inx = 0; inx < bytes.length; inx++)
        {
            bytes[ inx ] = (byte) (bytes[ inx ] ^ (0x0f & dezi[ inx % 4 ]));
        }

        return new String(bytes);
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

    public static void makeToast(String text)
    {
        if (appHandler != null)
        {
            final String phtext = text;

            appHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(anyContext, phtext, Toast.LENGTH_LONG).show();
                }
            });
        }
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

            return content;
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

    //region All purpose simple getters

    public static boolean hasNavigationBar()
    {
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

        return ! (hasBackKey && hasHomeKey);
    }

    public static int getDP(int pixels)
    {
        return (int) ((pixels / (float) getDeviceDPI()) * 160);
    }

    public static float getDeviceTextSize(float textsize)
    {
        return (textsize / getDeviceDPI()) * 160;
    }

    @Nullable
    public static String getMatch(String regex, String content)
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        if (! matcher.find()) return null;

        return matcher.group(1);
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

    public static String[] getTransArray(int resid)
    {
        return appContext.getResources().getStringArray(resid);
    }

    public static List<String> getTransList(int resid)
    {
        String[] array = getTransArray(resid);
        List<String> list = new ArrayList<>();

        for (int inx = 0; inx < array.length; inx++) list.add(array[ inx ]);

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

        Map<String, String> map = new LinkedHashMap<String, String>();

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

    public static File getMediaDirType(String dirtype)
    {
        return Environment.getExternalStoragePublicDirectory(dirtype);
    }

    public static void makeDirectory(File path)
    {
        //noinspection ResultOfMethodCallIgnored
        path.mkdirs();
    }

    public static void makeDirectory(String path)
    {
        makeDirectory(new File(path));
    }

    public static File getMediaPath(String disposition)
    {
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

        if (disposition.equals("misc"))
        {
            File dir = getMediaDirType(Environment.DIRECTORY_DCIM);
            return new File(dir, "Miscellanous");
        }

        if (disposition.equals("download"))
        {
            return getMediaDirType(Environment.DIRECTORY_DOWNLOADS);
        }

        if (disposition.equals("whatsapp"))
        {
            File dir = Environment.getExternalStorageDirectory();
            return new File(dir, "WhatsApp/Media/WhatsApp Images");
        }

        File dir = getMediaDirType(Environment.DIRECTORY_DCIM);
        return new File(dir, "Miscellanous");
    }

    public static File getFilesDir()
    {
        return Simple.getAnyContext().getFilesDir();
    }

    public static File getCacheDir()
    {
        return Simple.getAnyContext().getCacheDir();
    }

    public static String getTempfile(String filename)
    {
        return new File(Simple.getAnyContext().getCacheDir(), filename).toString();
    }

    public static long getFilesize(String filepath)
    {
        if (filepath == null) return 0;
        File file = new File(filepath);
        if (! file.exists()) return 0;

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

    public static int getDeviceDPI()
    {
        return anyContext.getResources().getDisplayMetrics().densityDpi;
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
        return (NotificationManager) anyContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static InputMethodManager getInputMethodManager()
    {
        return (InputMethodManager) anyContext.getSystemService(Context.INPUT_METHOD_SERVICE);
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
            WifiManager wifiManager = (WifiManager) anyContext.getSystemService(Context.WIFI_SERVICE);
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
    public static String getWifiIPAddress()
    {
        if (anyContext == null) return null;

        try
        {
            WifiManager wifiManager = (WifiManager) anyContext.getSystemService(Context.WIFI_SERVICE);
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
            WifiManager wifiManager = (WifiManager) anyContext.getSystemService(Context.WIFI_SERVICE);

            return wifiManager.getConnectionInfo().getSSID().replace("\"", "");
        }
        catch (Exception ignore)
        {
        }

        return null;
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

        WifiManager wifiManager = (WifiManager) anyContext.getSystemService(Context.WIFI_SERVICE);
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
        if (anyContext == null) return 16f;

        DisplayMetrics displayMetrics = anyContext.getResources().getDisplayMetrics();

        int pixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);

        if (pixels < 500) return 17f;

        return 24f;
    }

    public static float getPreferredTextSize()
    {
        if (anyContext == null) return 17f;

        DisplayMetrics displayMetrics = anyContext.getResources().getDisplayMetrics();

        int pixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);

        if (pixels < 500) return 17f;

        return 18f;
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
        return getHexBytesToString(bytes, 0, bytes.length);
    }

    public static String getHexBytesToString(byte[] bytes, int offset, int length)
    {
        if (bytes == null) return null;

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
    public static String getFileContent(File file)
    {
        try
        {
            InputStream in = new FileInputStream(file);
            int len = (int) file.length();
            byte[] buf = new byte[ len ];

            int xfer = 0;
            while (xfer < len) xfer += in.read(buf, xfer, len - xfer);
            in.close();

            return new String(buf);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    public static boolean putFileContent(File file, String content)
    {
        try
        {
            OutputStream out = new FileOutputStream(file);
            out.write(content.getBytes());
            out.close();

            return true;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return false;
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
        Bitmap myBitmap = getBitmapFromFile(file);
        if (myBitmap == null) return null;

        return new BitmapDrawable(appContext.getResources(), myBitmap);
    }

    @Nullable
    public static Bitmap getBitmapFromFile(String file)
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

    public static Bitmap getBitmapFromResource(int resid)
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
        if (! matcher.find()) return null;

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

        return new byte[]{buffer.array()[0], buffer.array()[1]};
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
            reversedArray[ (len - index) - 1 ] = array[index];
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

    public static String JSONdefuck(String json)
    {
        //
        // I hate slash escaping.
        //

        return json.replace("\\/","/");
    }

    //endregion JSON stuff

    //region Date and time

    private static final String ISO8601DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ISO8601DATENOSECS = "yyyy-MM-dd'T'HH:mm'Z'";

    public static long nowAsTimeStamp()
    {
        return new Date().getTime();
    }

    public static String nowAsISO()
    {
        return timeStampAsISO(nowAsTimeStamp());
    }

    @Nullable
    public static String timeStampAsISO(long timestamp)
    {
        DateFormat df = new SimpleDateFormat(ISO8601DATEFORMAT, Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date(timestamp));
    }

    @Nullable
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
            OopsService.log(LOGTAG, ex);
        }

        return 0;
    }

    public static String getLocalDay(long timeStamp)
    {
        DateFormat df = new SimpleDateFormat("EEEE", Locale.getDefault());
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
        if ((dir == null) || ! dir.isDirectory()) return null;

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

    public static class IsFileFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return new File(dir, name).isFile();
        }
    }

    public static class IsDirFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return new File(dir, name).isDirectory();
        }
    }

    public static class ImageFileFilter implements FilenameFilter
    {
        private final String[] exts =  new String[] {".jpg", ".png", ".gif", ".jpeg"};

        public boolean accept(File dir, String name)
        {
            if (new File(dir, name).isFile())
            {
                for (String ext : exts)
                {
                    if (name.toLowerCase().endsWith(ext))
                    {
                        return true;
                    }
                }
            }

            return false;
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
        return getSharedPrefs().getInt(key, 0);
    }

    public static boolean getSharedPrefBoolean(String key)
    {
        return getSharedPrefs().getBoolean(key, false);
    }

    @Nullable
    public static String getSharedPrefString(String key)
    {
        return getSharedPrefs().getString(key, null);
    }

    @Nullable
    public static Set<String> getSharedPrefStringSet(String key)
    {
        return getSharedPrefs().getStringSet(key, null);
    }

    public static void setSharedPrefString(String key, String value)
    {
        getSharedPrefs().edit().putString(key, value).apply();
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

    public static void removeSharedPref(String key)
    {
        Log.w(LOGTAG, "removeSharedPref: " + key);

        Simple.getSharedPrefs().edit().remove(key).commit();
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
}
