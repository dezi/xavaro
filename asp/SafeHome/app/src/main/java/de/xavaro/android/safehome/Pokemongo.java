package de.xavaro.android.safehome;


import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.PokemonImage;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.PokemonDecode;

public class Pokemongo extends FrameLayout
{
    private static final String LOGTAG = "POKEDEZI";

    private static final int numPokemons = 151;

    private WindowManager.LayoutParams overlayParam;

    private final static String[] commandModeTexts = new String[]{"STOP", "SEEK", "HUNT", "SHOP"};

    private final static FrameLayout[] cmdButtons = new FrameLayout[ 3 ];
    private final static TextView[] cmdButtonTextViews = new TextView[ 3 ];
    private final static ImageView[] cmdButtonImageViews = new ImageView[ 3 ];
    private final static String[] cmdTexts = new String[]{"HIDE", "SHOP", "HUNT"};

    private final static FrameLayout[] dirButtons = new FrameLayout[ 9 ];
    private final static TextView[] dirButtonTextViews = new TextView[ 9 ];
    private final static String[] dirTexts = new String[]{"NW", "N", "NE", "W", "STOP", "E", "SW", "S", "SE"};
    private final static int[] dirMoveX = new int[]{-1, 0, 1, -1, 0, 1, -1, 0, 1};
    private final static int[] dirMoveY = new int[]{1, 1, 1, 0, 0, 0, -1, -1, -1};

    private final static FrameLayout[] spawns = new FrameLayout[ 27 ];
    private final static ImageView[] pimages = new ImageView[ 27 ];
    private final static JSONObject[] locsJson = new JSONObject[ 27 ];

    private final static int buttsize = 56;
    private final static int buttpad = 4;
    private final static int buttnetto = buttsize - buttpad * 2;

    private final static int xsize = buttsize * ((cmdButtons.length + dirButtons.length + spawns.length) / 3);
    private final static int ysize = buttsize * 3;

    private final static JSONObject pokeLocs = new JSONObject();
    private final static JSONObject pokeLocsDead = new JSONObject();

    private static FrameLayout pokeDir;
    private static FrameLayout[] pokeDirFrames;
    private static ImageView[] pokeDirImages;
    private static boolean[] pokeDirEnabled;
    private static boolean[] pokeDirHunting;
    private static int pokeDirCols;
    private static int pokeDirRows;

    private static JSONObject poke2spawn = new JSONObject();

    private Pokemongo(Context context)
    {
        super(context);

        overlayParam = new WindowManager.LayoutParams(
                xsize, ysize,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        overlayParam.gravity = Gravity.TOP + Gravity.CENTER_HORIZONTAL;

        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .addView(this, overlayParam);

        for (int inx = 0; inx < cmdButtons.length; inx++)
        {
            cmdButtons[ inx ] = new FrameLayout(context);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(buttnetto, buttnetto);
            lp.gravity = Gravity.TOP + Gravity.LEFT;

            lp.topMargin = ((inx / (cmdButtons.length / 3)) * buttsize) + buttpad;
            lp.leftMargin = ((inx % (cmdButtons.length / 3)) * buttsize) + buttpad;

            cmdButtons[ inx ].setLayoutParams(lp);

            final int buttinx = inx;

            cmdButtons[ inx ].setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onClickCommandButton(buttinx);
                }
            });

            addView(cmdButtons[ inx ]);

            cmdButtonTextViews[ inx ] = new TextView(context);

            cmdButtonTextViews[ inx ].setText(cmdTexts[ inx ]);
            cmdButtonTextViews[ inx ].setTypeface(null, Typeface.NORMAL);
            cmdButtonTextViews[ inx ].setBackgroundColor(0x88000088);
            cmdButtonTextViews[ inx ].setTextSize(buttnetto / 3.0f);
            cmdButtonTextViews[ inx ].setGravity(Gravity.CENTER_HORIZONTAL + Gravity.CENTER_VERTICAL);

            cmdButtons[ inx ].addView(cmdButtonTextViews[ inx ]);

            cmdButtonImageViews[ inx ] = new ImageView(context);
            cmdButtons[ inx ].addView(cmdButtonImageViews[ inx ]);
        }

        for (int inx = 0; inx < 9; inx++)
        {
            dirButtons[ inx ] = new FrameLayout(context);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(buttnetto, buttnetto);
            lp.gravity = Gravity.TOP + Gravity.LEFT;

            lp.topMargin = ((inx / (dirButtons.length / 3)) * buttsize) + buttpad;
            lp.leftMargin = ((cmdButtons.length / 3) * buttsize) + ((inx % (dirButtons.length / 3)) * buttsize) + buttpad;

            dirButtons[ inx ].setLayoutParams(lp);

            final int buttinx = inx;

            dirButtons[ inx ].setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onClickJoystickButton(buttinx);
                }
            });

            addView(dirButtons[ inx ]);

            dirButtonTextViews[ inx ] = new TextView(context);

            dirButtonTextViews[ inx ].setText(dirTexts[ inx ]);
            dirButtonTextViews[ inx ].setTypeface(null, Typeface.NORMAL);
            dirButtonTextViews[ inx ].setBackgroundColor(0x88008800);
            dirButtonTextViews[ inx ].setTextSize(buttnetto / 3.0f);
            dirButtonTextViews[ inx ].setGravity(Gravity.CENTER_HORIZONTAL + Gravity.CENTER_VERTICAL);

            if (inx == 4)
            {
                dirButtonTextViews[ inx ].setTypeface(null, Typeface.BOLD);
                dirButtonTextViews[ inx ].setBackgroundColor(0xff008800);
            }

            dirButtons[ inx ].addView(dirButtonTextViews[ inx ]);
        }

        for (int inx = 0; inx < spawns.length; inx++)
        {
            spawns[ inx ] = new FrameLayout(context);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(buttnetto, buttnetto);
            lp.gravity = Gravity.TOP + Gravity.LEFT;

            lp.topMargin = ((inx / (spawns.length / 3)) * buttsize) + buttpad;
            lp.leftMargin = (((cmdButtons.length + dirButtons.length) / 3) * buttsize) + ((inx % (spawns.length / 3)) * buttsize) + buttpad;

            spawns[ inx ].setLayoutParams(lp);

            final int buttinx = inx;

            spawns[ inx ].setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onClickSpawnsButton(buttinx);
                }
            });

            addView(spawns[ inx ]);

            pimages[ inx ] = new ImageView(context);
            pimages[ inx ].setBackgroundColor(0xffffffff);

            spawns[ inx ].addView(pimages[ inx ]);
        }

        createPokemonDir();

        Log.d(LOGTAG, "Added system alert window...");
    }

    private void createPokemonDir()
    {
        pokeDirEnabled = new boolean[ numPokemons ];
        pokeDirHunting = new boolean[ numPokemons ];

        loadPokeHuntSettings();

        pokeDir = new FrameLayout(getContext());
        pokeDir.setBackgroundColor(0xffffffff);
        pokeDir.setVisibility(GONE);

        pokeDirFrames = new FrameLayout[ numPokemons ];
        pokeDirImages = new ImageView[ numPokemons ];

        pokeDirCols = (cmdButtons.length + dirButtons.length + spawns.length) / 3;
        pokeDirRows = (numPokemons / pokeDirCols) + (((numPokemons % pokeDirCols) > 0) ? 1 : 0);

        int width = pokeDirCols * buttsize;
        int height = pokeDirRows * buttsize;

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
        lp.topMargin = (3 * buttsize) + buttpad;

        pokeDir.setLayoutParams(lp);
        this.addView(pokeDir);

        for (int inx = 0; inx < numPokemons; inx++)
        {
            lp = new FrameLayout.LayoutParams(buttnetto, buttnetto);
            lp.topMargin = ((inx / pokeDirCols) * buttsize) + buttpad;
            lp.leftMargin = ((inx % pokeDirCols) * buttsize) + buttpad;

            pokeDirFrames[ inx ] = new FrameLayout(getContext());
            pokeDirFrames[ inx ].setLayoutParams(lp);

            pokeDir.addView(pokeDirFrames[ inx ]);

            pokeDirImages[ inx ] = new ImageView(getContext());
            pokeDirImages[ inx ].setBackgroundColor(0xffffffff);
            pokeDirImages[ inx ].setImageBitmap(PokemonImage.getPokemonImage(inx + 1));

            final int buttinx = inx;

            pokeDirImages[ inx ].setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onClickHuntPokemonButton(buttinx);
                }
            });

            pokeDirFrames[ inx ].addView(pokeDirImages[ inx ]);
        }
    }

    private static void buildPokeHuntSpawns()
    {
        synchronized (huntPointsTodo)
        {
            try
            {
                huntPointsTodo.clear();

                Iterator<String> pokeIterator = poke2spawn.keys();

                while (pokeIterator.hasNext())
                {
                    String pokeId = pokeIterator.next();

                    String[] parts = pokeId.split("@");
                    if (parts.length != 2) continue;

                    int pokeNum = Integer.parseInt(parts[ 1 ], 10);

                    if (!pokeDirHunting[ pokeNum - 1 ]) continue;

                    JSONArray spawns = poke2spawn.getJSONArray(pokeId);

                    for (int inx = 0; inx < spawns.length(); inx++)
                    {
                        huntPointsTodo.add(spawns.getJSONObject(inx));
                    }
                }

                //
                // Shuffle list.
                //

                for (int inx = 0; inx < huntPointsTodo.size(); inx++)
                {
                    int shuffle = (int) (Math.random() * huntPointsTodo.size());

                    huntPointsTodo.add(huntPointsTodo.remove(shuffle));
                }

                Log.d(LOGTAG, "buildPokeHuntSpawns: size=" + huntPointsTodo.size());
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }
        }
    }

    private static void enablePokemonDirEntry(int pokeNum)
    {
        if ((pokeNum <= 0) || (pokeNum > numPokemons)) return;

        if (pokeDirHunting[ pokeNum - 1 ])
        {
            pokeDirImages[ pokeNum - 1 ].setBackgroundColor(0xffffcccc);
        }
        else
        {
            pokeDirImages[ pokeNum - 1 ].setBackgroundColor(0xcccccccc);
        }

        pokeDirEnabled[ pokeNum - 1 ] = true;
    }

    private void onClickHuntPokemonButton(int buttinx)
    {
        if (! pokeDirEnabled[ buttinx ]) return;

        if (pokeDirHunting[ buttinx ])
        {
            pokeDirImages[ buttinx ].setBackgroundColor(0xcccccccc);
            pokeDirHunting[ buttinx ] = false;
        }
        else
        {
            pokeDirImages[ buttinx ].setBackgroundColor(0xffffcccc);
            pokeDirHunting[ buttinx ] = true;
        }

        savePokeHuntSettings();
    }

    private void onClickCommandButton(int buttinx)
    {
        if (buttinx == 0)
        {
            if (overlayParam.width == xsize)
            {
                overlayParam.width = buttsize;
                overlayParam.height = buttsize;
                overlayParam.gravity = Gravity.TOP + Gravity.LEFT;
                cmdButtonTextViews[ 0 ].setText("SHOW");
            }
            else
            {
                overlayParam.width = xsize;
                overlayParam.height = ysize;
                overlayParam.gravity = Gravity.TOP + Gravity.CENTER_HORIZONTAL;
                cmdButtonTextViews[ 0 ].setText("HIDE");
            }
        }

        if (buttinx == 2)
        {
            if (isShowhunt)
            {
                overlayParam.height = ysize;
                pokeDir.setVisibility(GONE);
                isShowhunt = false;
            }
            else
            {
                overlayParam.height = ysize + (pokeDirRows * buttsize) + buttpad;
                pokeDir.setVisibility(VISIBLE);
                isShowhunt = true;
            }
        }

        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .removeView(this);

        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .addView(this, overlayParam);
    }

    private void onClickSpawnsButton(int buttinx)
    {
        synchronized (pokeLocs)
        {
            try
            {
                JSONObject gotoLoc = locsJson[ buttinx ];
                if (gotoLoc == null) return;

                JSONObject locs = gotoLoc.getJSONObject("loc");

                Log.d(LOGTAG, "onClickSpawnsButton: " + locs.toString(2));

                Iterator<String> locsIterator = locs.keys();
                if (! locsIterator.hasNext()) return;

                String latlonstr = locsIterator.next();
                locs.remove(latlonstr);

                pimages[ buttinx ].setBackgroundColor(0xcccccccc);

                long expires = new Date().getTime() + 60 * 1000;
                pokeLocsDead.put(latlonstr, expires);

                spotLocation = latlonstr;
                suspendTime = new Date().getTime() + 15 * 1000;
                isSpotting = true;
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }
        }
    }

    private static int commandMode;
    private static long suspendTime;

    private static boolean isSpotting;
    private static boolean isShowhunt;

    private static String spotLocation;

    private void onClickJoystickButton(int buttinx)
    {
        if (buttinx == 4)
        {
            commandMode = ++commandMode % commandModeTexts.length;
            dirButtonTextViews[ 4 ].setText(commandModeTexts[ commandMode ]);

            if (commandMode == 2)
            {
                synchronized (huntPointsTodo)
                {
                    huntPointsTodo.clear();
                }
            }

            latMove = 0;
            lonMove = 0;
        }

        if (dirMoveY[ buttinx ] == 0) latMove = 0;
        if (dirMoveX[ buttinx ] == 0) lonMove = 0;

        latMove += dirMoveY[ buttinx ] / 10000.0;
        lonMove += dirMoveX[ buttinx ] / 10000.0;

        if (Math.abs(latMove) < 0.00000001) latMove = 0;
        if (Math.abs(lonMove) < 0.00000001) lonMove = 0;

        if ((latMove == 0) && (lonMove == 0))
        {
            buttinx = 4;
        }

        Log.d(LOGTAG, "Alert butt touch:"
                + " buttinx=" + buttinx
                + " latMove=" + String.format(Locale.ROOT, "%.6f", latMove)
                + " lonMove=" + String.format(Locale.ROOT, "%.6f", lonMove));

        for (int inx = 0; inx < dirButtonTextViews.length; inx++)
        {
            if (inx == buttinx)
            {
                dirButtonTextViews[ inx ].setTypeface(null, Typeface.BOLD);
                dirButtonTextViews[ inx ].setBackgroundColor(0xff008800);
            }
            else
            {
                dirButtonTextViews[ inx ].setText(dirTexts[ inx ]);
                dirButtonTextViews[ inx ].setTypeface(null, Typeface.NORMAL);
                dirButtonTextViews[ inx ].setBackgroundColor(0x88008800);
            }
        }

    }

    private static Pokemongo instance;
    private static Application application;

    private static void initPokemongo(Location location)
    {
        if (instance != null) return;

        if (lat == 0) lat = location.getLatitude();
        if (lon == 0) lon = location.getLongitude();

        try
        {
            application = getApplicationUsingReflection();
            instance = new Pokemongo(application);
        }
        catch (Exception ex)
        {
            Log.d(LOGTAG, "Init failed...");
            ex.printStackTrace();
        }

        loadPokeSpawnMap();
    }

    private static Application getApplicationUsingReflection() throws Exception
    {
        return (Application) Class.forName("android.app.AppGlobals")
                .getMethod("getInitialApplication").invoke(null, (Object[]) null);
    }

    private void sampleCalls(Location location)
    {
        deziLocation(location);

        byte[] data = new byte[ 1 ];
        int offset = 1;
        int size = 2;
        ByteBuffer buffer = null;
        String url = "url";

        pokeWriteText(url);
        pokeOpenFile(url);
        pokeWriteBytes(url, data, offset, size);
        pokeWriteBuffer(url, buffer, offset, size);
        pokeCloseFile(url);
    }

    //
    // Hamburg => 53.55, 10.00
    //

    private static double lat = 53.55 + ((Math.random() - 0.5) / 50.0);
    private static double lon = 10.00 + ((Math.random() - 0.5) / 50.0);

    private static long spawnCount = 0;

    private static double latMove = 0;
    private static double lonMove = 0;

    private static void setCommand(int command)
    {
        commandMode = command;

        for (int inx = 0; inx < dirButtonTextViews.length; inx++)
        {
            if (inx == 4)
            {
                dirButtonTextViews[ inx ].setText(commandModeTexts[ command ]);
                dirButtonTextViews[ inx ].setTypeface(null, Typeface.BOLD);
                dirButtonTextViews[ inx ].setBackgroundColor(0xff008800);
            }
            else
            {
                dirButtonTextViews[ inx ].setTypeface(null, Typeface.NORMAL);
                dirButtonTextViews[ inx ].setBackgroundColor(0x88008800);
            }
        }
    }

    private static void clearFinalJSON(JSONObject json)
    {
        try
        {
            Iterator<String> keysIterator = json.keys();

            while (keysIterator.hasNext())
            {
                json.remove(keysIterator.next());
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    public static void deziLocation(Location location)
    {
        initPokemongo(location);

        setupSpawns();

        if (isSpotting)
        {
            try
            {
                JSONObject latlon = new JSONObject(spotLocation);

                latMove = 0;
                lonMove = 0;

                lat = latlon.getDouble("lat");
                lon = latlon.getDouble("lon");

                Log.d(LOGTAG, "deziLocation: seeking:"
                        + " lat=" + location.getLatitude()
                        + " lon=" + location.getLongitude());
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }

            isSpotting = false;
        }

        if (suspendTime < new Date().getTime())
        {
            if (commandMode == 1)
            {
                if ((spawnCount++ % 2) == 0)
                {
                    if (spawnPointsTodo.size() > 0)
                    {
                        try
                        {
                            String spanposstr = spawnPointsTodo.remove(0);
                            spawnPointsSeen.add(spanposstr);

                            JSONObject spanpos = new JSONObject(spanposstr);

                            lat = spanpos.getDouble("lat");
                            lon = spanpos.getDouble("lon");

                            Log.d(LOGTAG, "deziLocation: spawn"
                                    + " lat=" + location.getLatitude()
                                    + " lon=" + location.getLongitude());
                        }
                        catch (Exception ignore)
                        {
                        }
                    }
                }
            }

            if (commandMode == 2)
            {
                if (huntPointsTodo.size() == 0)
                {
                    synchronized (pokeLocs)
                    {
                        clearFinalJSON(pokeLocs);
                        clearFinalJSON(pokeLocsDead);
                    }

                    setupSpawns();

                    buildPokeHuntSpawns();
                }

                if ((spawnCount++ % 2) == 0)
                {
                    if (huntPointsTodo.size() > 0)
                    {
                        try
                        {
                            synchronized (huntPointsTodo)
                            {
                                JSONObject huntPoint = huntPointsTodo.remove(0);
                                huntPointsTodo.add(huntPoint);

                                lat = huntPoint.getDouble("lat");
                                lon = huntPoint.getDouble("lon");
                            }

                            Log.d(LOGTAG, "deziLocation: hunt"
                                    + " lat=" + location.getLatitude()
                                    + " lon=" + location.getLongitude());
                        }
                        catch (Exception ignore)
                        {
                        }
                    }
                }
            }

            lat += latMove;
            lon += lonMove;
        }

        location.setLatitude(lat);
        location.setLongitude(lon);

        Log.d(LOGTAG, "deziLocation:"
                + " lat=" + location.getLatitude()
                + " lon=" + location.getLongitude()
                + " latMove=" + String.format(Locale.ROOT, "%.6f", latMove)
                + " lonMove=" + String.format(Locale.ROOT, "%.6f", lonMove)
        );
    }

    private static final Map<Integer, OutputStream> outputs = new HashMap<>();
    private static final Map<Integer, byte[]> postdata = new HashMap<>();
    private static final ArrayList<File> logfiles = new ArrayList<>();
    private static final ArrayList<String> pokeposen = new ArrayList<>();

    private static final ArrayList<JSONObject> huntPointsTodo = new ArrayList<>();
    private static final ArrayList<String> spawnPointsTodo = new ArrayList<>();
    private static final ArrayList<String> spawnPointsSeen = new ArrayList<>();

    // http://www.pokemon.com/de/api/pokedex/kalos
    // http://www.pokemon.com/us/api/pokedex/kalos
    // http://assets.pokemon.com/assets/cms2/img/pokedex/full/010.png

    public static void pokeOpenFile(String url)
    {
        int urlhash = System.identityHashCode(url);

        Log.d(LOGTAG, "pokeOpenFile url=" + url + " hash=" + urlhash);

        if (!url.contains("pgorelease.nianticlabs.com")) return;

        while (logfiles.size() > 10)
        {
            File tobedel = logfiles.remove(0);
            tobedel.delete();
        }

        DateFormat df = new SimpleDateFormat("yyyyMMdd'.'HHmmss", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String filename = "pm." + df.format(new Date()) + ".json";

        File extstore = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File extdir = new File(extstore, "pm");
        if (!extdir.exists()) extdir.mkdirs();
        File extfile = new File(extdir, filename);

        if (extfile.exists())
        {
            Log.d(LOGTAG, "pokeOpenFile toofast... url=" + url + " hash=" + urlhash);

            return;
        }

        //Log.d(LOGTAG, "pokeOpenFile url=" + url + " file=" + extfile.toString());

        try
        {
            OutputStream out = new FileOutputStream(extfile);

            //out.write(url.getBytes());
            //out.write("\n--\n".getBytes());

            outputs.put(urlhash, out);
            postdata.put(urlhash, new byte[ 0 ]);

            logfiles.add(extfile);
        }
        catch (Exception ignore)
        {
        }
    }

    public static void pokeWriteText(String text)
    {
        //Log.d(LOGTAG, "pokeWriteText: text=" + text);
    }

    public static void pokeWriteBytes(String url, byte[] buffer, int offset, int count)
    {
        int urlhash = System.identityHashCode(url);

        //Log.d(LOGTAG, "pokeWriteBytes: offset=" + offset + " len=" + count + " hash=" + urlhash);

        OutputStream out = outputs.get(urlhash);
        byte[] request = postdata.get(urlhash);

        if (out != null)
        {
            try
            {
                //Log.d(LOGTAG, "pokeWriteBytes: found... offset=" + offset + " len=" + count);

                //out.write(buffer, offset, count);

                byte[] newdata = new byte[ request.length + (count - offset) ];

                System.arraycopy(request, 0, newdata, 0, request.length);
                System.arraycopy(buffer, offset, newdata, request.length, count - offset);

                postdata.put(urlhash, newdata);
            }
            catch (Exception ignore)
            {
            }
        }
    }

    public static void pokeWriteBuffer(String url, ByteBuffer buffer, int offset, int count)
    {
        int urlhash = System.identityHashCode(url);

        //Log.d(LOGTAG, "pokeWriteBuffer: offset=" + offset + " len=" + count + " hash=" + urlhash);

        OutputStream out = outputs.get(urlhash);
        byte[] request = postdata.get(urlhash);

        if (out != null)
        {
            try
            {
                //Log.d(LOGTAG, "pokeWriteBuffer: found... offset=" + offset + " len=" + count);

                //out.write("\n--\n".getBytes());

                if ((buffer != null) && buffer.hasArray())
                {
                    byte[] response = new byte[ count ];
                    System.arraycopy(buffer.array(), buffer.arrayOffset() + offset, response, 0, count);

                    //out.write(response);

                    PokemonDecode pd = new PokemonDecode();

                    JSONObject result = pd.decode(url, request, response);

                    if (result != null)
                    {
                        String jres = result.toString(2);

                        //out.write("\n--\n".getBytes());
                        out.write(jres.replace("\\/", "/").getBytes());

                        saveRecords(result);
                        evalMapDetails(result);
                        evalGymDetails(result);
                        evalFortDetails(result);
                    }
                }
            }
            catch (Exception ignore)
            {
            }
        }
    }

    public static void pokeCloseFile(String url)
    {
        int urlhash = System.identityHashCode(url);

        //Log.d(LOGTAG, "pokeCloseFile: hash=" + urlhash);

        OutputStream out = outputs.get(urlhash);

        if (out != null)
        {
            try
            {
                //Log.d(LOGTAG, "pokeCloseFile: closed... out=" + out + " hash=" + urlhash);

                out.close();

                outputs.remove(urlhash);
                postdata.remove(urlhash);
            }
            catch (Exception ignore)
            {
            }
        }
    }

    private static void saveRecords(JSONObject json)
    {
        File extdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        try
        {
            JSONObject rpcrequest = json.getJSONObject("request");
            JSONArray requests = rpcrequest.getJSONArray("requests@.POGOProtos.Networking.Requests.Request");

            for (int rinx = 0; rinx < requests.length(); rinx++)
            {
                JSONObject request = requests.getJSONObject(rinx);

                String restype = request.getString("request_type@.POGOProtos.Networking.Requests.RequestType");

                Log.d(LOGTAG, "saveRecords: req type=" + restype);

                String file = "pm.Requests." + CamelName(restype) + "Response.json";
                File extfile = new File(extdir, file);

                Log.d(LOGTAG, "saveRecords: req name=" + extfile.toString());

                OutputStream out = new FileOutputStream(extfile);
                out.write(request.toString(2).replace("\\/", "/").getBytes());
                out.close();
            }

            JSONObject response = json.getJSONObject("response");
            JSONArray returns = response.getJSONArray("returns@array");

            for (int inx = 0; inx < returns.length(); inx++)
            {
                JSONObject returnobj = returns.getJSONObject(inx);
                String type = returnobj.getString("type");
                JSONObject data = returnobj.getJSONObject("data");

                Log.d(LOGTAG, "saveRecords: res type=" + type);

                String[] parts = type.split("\\.");
                if (parts.length < 3) continue;
                String file = "pm." + parts[ parts.length - 2 ] + "." + parts[ parts.length - 1 ] + ".json";
                File extfile = new File(extdir, file);

                Log.d(LOGTAG, "saveRecords: res name=" + extfile.toString());

                OutputStream out = new FileOutputStream(extfile);
                out.write(data.toString(2).replace("\\/", "/").getBytes());
                out.close();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static String CamelName(String uppercase)
    {
        String camelname = "";
        boolean nextUp = true;

        for (int inx = 0; inx < uppercase.length(); inx++)
        {
            if (uppercase.charAt(inx) == '@') break;

            if (uppercase.charAt(inx) == '_')
            {
                nextUp = true;
                continue;
            }

            camelname += nextUp ? uppercase.charAt(inx) : Character.toLowerCase(uppercase.charAt(inx));
            nextUp = false;
        }

        return camelname;
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
        catch (Exception ignore)
        {
        }

        return false;
    }

    private static void loadPokeHuntSettings()
    {
        try
        {
            File extdir = Environment.getExternalStorageDirectory();
            File extpoke = new File(extdir, "Mongopoke");

            if (extpoke.exists())
            {
                File extfile = new File(extpoke, "Settings.PokeHunt.json");

                FileInputStream input = new FileInputStream(extfile);
                int size = (int) input.getChannel().size();
                byte[] content = new byte[ size ];
                int xfer = input.read(content);
                input.close();

                if (size == xfer)
                {
                    JSONArray hunts = new JSONArray(new String(content));

                    for (int inx = 0; inx < hunts.length(); inx++)
                    {
                        pokeDirHunting[ inx ] = hunts.getBoolean(inx);
                    }
                }
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    private static void savePokeHuntSettings()
    {
        try
        {
            File extdir = Environment.getExternalStorageDirectory();
            File extpoke = new File(extdir, "Mongopoke");

            if (extpoke.exists() || extpoke.mkdir())
            {
                JSONArray hunts = new JSONArray();

                for (boolean hunt : pokeDirHunting)
                {
                    hunts.put(hunt);
                }

                File extfile = new File(extpoke, "Settings.PokeHunt.json");

                OutputStream out = new FileOutputStream(extfile);
                out.write(hunts.toString(2).replace("\\/", "/").getBytes());
                out.close();
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    private static void loadPokeSpawnMap()
    {
        try
        {
            File extdir = Environment.getExternalStorageDirectory();
            File extpoke = new File(extdir, "Mongopoke");

            Log.d(LOGTAG, "loadPokeSpawnMap: dir=" + extpoke.toString());

            if (extpoke.exists())
            {
                File extspawn = new File(extpoke, "Harvest.SpawnMap.json");

                Log.d(LOGTAG, "loadPokeSpawnMap: fil=" + extspawn.toString());

                FileInputStream input = new FileInputStream(extspawn);
                int size = (int) input.getChannel().size();
                byte[] content = new byte[ size ];
                int xfer = input.read(content);
                input.close();

                if (size == xfer)
                {
                    poke2spawn = new JSONObject(new String(content));

                    Iterator<String> keysIterator = poke2spawn.keys();

                    while (keysIterator.hasNext())
                    {
                        String pokeId = keysIterator.next();

                        String[] parts = pokeId.split("@");
                        if (parts.length != 2) continue;

                        int pokeNum = Integer.parseInt(parts[ 1 ], 10);
                        enablePokemonDirEntry(pokeNum);
                    }
                }
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }

        if (poke2spawn == null) poke2spawn = new JSONObject();
    }

    private static void savePokeSpawnMap()
    {
        try
        {
            File extdir = Environment.getExternalStorageDirectory();
            File extpoke = new File(extdir, "Mongopoke");

            Log.d(LOGTAG, "savePokeSpawnMap: dir=" + extpoke.toString());

            if (extpoke.exists() || extpoke.mkdir())
            {
                File extspawn = new File(extpoke, "Harvest.SpawnMap.json");

                Log.d(LOGTAG, "savePokeSpawnMap: fil=" + extspawn.toString());

                OutputStream out = new FileOutputStream(extspawn);
                out.write(poke2spawn.toString(2).replace("\\/", "/").getBytes());
                out.close();
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    private static void addPokeSpawnMap(JSONObject poke)
    {
        try
        {
            String spid = poke.getString("spawn_point_id@string");
            double plat = poke.getDouble("latitude@double");
            double plon = poke.getDouble("longitude@double");

            String pokeId = null;

            if (poke.has("pokemon_id@.POGOProtos.Enums.PokemonId"))
            {
                pokeId = poke.getString("pokemon_id@.POGOProtos.Enums.PokemonId");
            }

            if (poke.has("pokemon_data@.POGOProtos.Data.PokemonData"))
            {
                JSONObject pokedata = poke.getJSONObject("pokemon_data@.POGOProtos.Data.PokemonData");
                pokeId = pokedata.getString("pokemon_id@.POGOProtos.Enums.PokemonId");
            }

            if (pokeId == null) return;

            if (! poke2spawn.has(pokeId)) poke2spawn.put(pokeId, new JSONArray());
            JSONArray spawns = poke2spawn.getJSONArray(pokeId);

            if (spawns.length() > 25) return;

            for (int inx = 0; inx < spawns.length(); inx++)
            {
                JSONObject spoint = spawns.getJSONObject(inx);

                if (spid.equals(spoint.getString("sid")))
                {
                    //
                    // Point is known.
                    //

                    return;
                }
            }

            JSONObject spoint = new JSONObject();
            spoint.put("sid", spid);
            spoint.put("lat", plat);
            spoint.put("lon", plon);

            spawns.put(spoint);

            savePokeSpawnMap();

            String[] parts = pokeId.split("@");
            if (parts.length != 2) return;

            int pokeNum = Integer.parseInt(parts[ 1 ], 10);
            enablePokemonDirEntry(pokeNum);
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    private static void addPokepos(String tag, JSONObject poke)
    {
        try
        {
            addPokeSpawnMap(poke);

            double plat = poke.getDouble("latitude@double");
            double plon = poke.getDouble("longitude@double");

            String pokeId = null;

            if (poke.has("pokemon_id@.POGOProtos.Enums.PokemonId"))
            {
                pokeId = poke.getString("pokemon_id@.POGOProtos.Enums.PokemonId");
            }

            if (poke.has("pokemon_data@.POGOProtos.Data.PokemonData"))
            {
                JSONObject pokedata = poke.getJSONObject("pokemon_data@.POGOProtos.Data.PokemonData");
                pokeId = pokedata.getString("pokemon_id@.POGOProtos.Enums.PokemonId");
            }

            if (pokeId == null) return;

            long tthidden = -1;

            if (poke.has("time_till_hidden_ms@int32"))
            {
                tthidden = poke.getInt("time_till_hidden_ms@int32");
            }

            if (poke.has("expiration_timestamp_ms@int64"))
            {
                long expire = poke.getLong("expiration_timestamp_ms@int64");
                long now = new Date().getTime();

                tthidden = expire - now;
            }

            JSONObject pokepos = new JSONObject();
            pokepos.put("lat", plat);
            pokepos.put("lon", plon);

            String pokeposstr = pokepos.toString();

            if (tthidden < 0)
            {
                Log.d(LOGTAG, "addPokepos: bad expiration: tag=" + tag + " id=" + pokeId + " tth=" + tthidden);
            }

            String[] parts = pokeId.split("@");
            if (parts.length != 2) return;
            int pokeOrd = Integer.parseInt(parts[ 1 ], 10);

            if ((commandMode == 2) && ! pokeDirHunting[ pokeOrd - 1 ])
            {
                //
                // Check if already known. If not, we accept new
                // pokemons positions anyway.
                //

                if (pokeDirEnabled[ pokeOrd - 1 ]) return;
            }

            synchronized (pokeLocs)
            {
                if (! pokeLocsDead.has(pokeposstr))
                {
                    if (!pokeLocs.has(pokeId)) pokeLocs.put(pokeId, new JSONObject());
                    JSONObject pokeLoc = pokeLocs.getJSONObject(pokeId);

                    pokeLoc.put("pid", pokeId);
                    pokeLoc.put("ord", pokeOrd);

                    if (!pokeLoc.has("loc")) pokeLoc.put("loc", new JSONObject());
                    JSONObject loc = pokeLoc.getJSONObject("loc");

                    long expires = new Date().getTime() + tthidden;
                    loc.put(pokeposstr, expires);
                }
            }

            Log.d(LOGTAG, "addPokepos: tag=" + tag + " id=" + pokeId + " tth=" + (tthidden / 1000) + " chk=" + pokeposstr);
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    private static void registerSpawn(JSONObject spawn)
    {
        try
        {
            double plat = spawn.getDouble("latitude@double");
            double plon = spawn.getDouble("longitude@double");

            JSONObject spawnpos = new JSONObject();
            spawnpos.put("lat", plat);
            spawnpos.put("lon", plon);

            String spawnposstr = spawnpos.toString();

            if ((! spawnPointsTodo.contains(spawnposstr))
                    && (! spawnPointsSeen.contains(spawnposstr)))
            {
                spawnPointsTodo.add(spawnposstr);
            }
        }
        catch (Exception ignore)
        {
        }
    }

    private static void evalMapDetails(JSONObject json)
    {
        try
        {
            JSONObject response = json.getJSONObject("response");
            JSONArray returns = response.getJSONArray("returns@array");

            for (int inx = 0; inx < returns.length(); inx++)
            {
                JSONObject returnobj = returns.getJSONObject(inx);
                String type = returnobj.getString("type");

                if (type.equals(".POGOProtos.Networking.Responses.GetMapObjectsResponse"))
                {
                    JSONObject data = returnobj.getJSONObject("data");
                    JSONArray mapcells = data.getJSONArray("map_cells@.POGOProtos.Map.MapCell");

                    for (int minx = 0; minx < mapcells.length(); minx++)
                    {
                        JSONObject mapcell = mapcells.getJSONObject(minx);

                        boolean haveSpawn = false;

                        if (mapcell.has("spawn_points@.POGOProtos.Map.SpawnPoint"))
                        {
                            JSONArray spawns = mapcell.getJSONArray("spawn_points@.POGOProtos.Map.SpawnPoint");

                            for (int sinx = 0; sinx < spawns.length(); sinx++)
                            {
                                JSONObject spawn = spawns.getJSONObject(sinx);
                                registerSpawn(spawn);
                                haveSpawn = true;
                                break;
                            }
                        }

                        if ((! haveSpawn) && mapcell.has("decimated_spawn_points@.POGOProtos.Map.SpawnPoint"))
                        {
                            JSONArray spawns = mapcell.getJSONArray("decimated_spawn_points@.POGOProtos.Map.SpawnPoint");

                            for (int sinx = 0; sinx < spawns.length(); sinx++)
                            {
                                JSONObject spawn = spawns.getJSONObject(sinx);
                                registerSpawn(spawn);

                                break;
                            }
                        }

                        if (mapcell.has("wild_pokemons@.POGOProtos.Map.Pokemon.WildPokemon"))
                        {
                            JSONArray pokes = mapcell.getJSONArray("wild_pokemons@.POGOProtos.Map.Pokemon.WildPokemon");

                            for (int winx = 0; winx < pokes.length(); winx++)
                            {
                                JSONObject poke = pokes.getJSONObject(winx);
                                addPokepos("wild", poke);
                            }
                        }

                        if (mapcell.has("catchable_pokemons@.POGOProtos.Map.Pokemon.MapPokemon"))
                        {
                            JSONArray pokes = mapcell.getJSONArray("catchable_pokemons@.POGOProtos.Map.Pokemon.MapPokemon");

                            for (int winx = 0; winx < pokes.length(); winx++)
                            {
                                JSONObject poke = pokes.getJSONObject(winx);
                                addPokepos("maps", poke);
                            }
                        }
                    }
                }
            }

        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    public static void setupSpawns()
    {
        synchronized (pokeLocs)
        {
            //
            // Remove expired dead spawns.
            //

            try
            {
                long now = new Date().getTime();

                Iterator<String> keysIterator = pokeLocsDead.keys();

                while (keysIterator.hasNext())
                {
                    String latlonstr = keysIterator.next();
                    long expiration = pokeLocsDead.getLong(latlonstr);
                    if (expiration < now)pokeLocsDead.remove(latlonstr);
                }
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }

            //
            // Remove expired spawns.
            //

            try
            {
                long now = new Date().getTime();

                Iterator<String> keysIterator = pokeLocs.keys();

                while (keysIterator.hasNext())
                {
                    String pokeId = keysIterator.next();

                    JSONObject pokeLoc = pokeLocs.getJSONObject(pokeId);
                    JSONObject locs = pokeLoc.getJSONObject("loc");

                    Iterator<String> locsIterator = locs.keys();

                    int valid = 0;

                    while (locsIterator.hasNext())
                    {
                        String latlonstr = locsIterator.next();
                        long expiration = locs.getLong(latlonstr);

                        if (expiration < now)
                        {
                            locs.remove(latlonstr);
                        }
                        else
                        {
                            valid++;
                        }
                    }

                    if (valid == 0) pokeLocs.remove(pokeId);
                }
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }

            //
            // Setup spawn dirButtons.
            //

            try
            {
                //Log.d(LOGTAG, "setupSpawns: " + pokeLocs.toString(2));

                int spawninx = 0;

                Iterator<String> keysIterator = pokeLocs.keys();

                while (keysIterator.hasNext())
                {
                    if (spawninx >= spawns.length) break;

                    String pokeId = keysIterator.next();

                    JSONObject pokeLoc = pokeLocs.getJSONObject(pokeId);
                    int pokeOrd = pokeLoc.getInt("ord");

                    Bitmap bitmap = PokemonImage.getPokemonImage(pokeOrd);

                    if (locsJson[ spawninx ] != pokeLoc)
                    {
                        locsJson[ spawninx ] = pokeLoc;

                        pimages[ spawninx ].setImageBitmap(bitmap);
                    }

                    pimages[ spawninx ].setBackgroundColor(0xffffffff);

                    spawninx++;
                }

                while (spawninx < spawns.length)
                {
                    locsJson[ spawninx ] = null;

                    pimages[ spawninx ].setImageBitmap(null);
                    pimages[ spawninx ].setBackgroundColor(0xffffffff);

                    spawninx++;
                }
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }
        }
    }

    private static void evalGymDetails(JSONObject json)
    {
        try
        {
            JSONObject response = json.getJSONObject("response");
            JSONArray returns = response.getJSONArray("returns@array");

            for (int inx = 0; inx < returns.length(); inx++)
            {
                JSONObject returnobj = returns.getJSONObject(inx);
                String type = returnobj.getString("type");

                if (type.equals(".POGOProtos.Networking.Responses.GetGymDetailsResponse"))
                {
                    JSONObject data = returnobj.getJSONObject("data");

                    JSONObject gymstate = data.getJSONObject("gym_state@.POGOProtos.Data.Gym.GymState");
                    JSONObject fortdata = gymstate.getJSONObject("fort_data@.POGOProtos.Map.Fort.FortData");

                    double latloc = fortdata.getDouble("latitude@double");
                    double lonloc = fortdata.getDouble("longitude@double");

                    Log.d(LOGTAG, "Gym encountered: lat=" + latloc + " lon=" + lonloc);

                    lat = latloc;
                    lon = lonloc;
                    latMove = 0;
                    lonMove = 0;

                    setCommand(0);
                }
            }
        }
        catch (Exception ex)
        {

        }
    }

    private static void evalFortDetails(JSONObject json)
    {
        try
        {
            JSONObject response = json.getJSONObject("response");
            JSONArray returns = response.getJSONArray("returns@array");

            for (int inx = 0; inx < returns.length(); inx++)
            {
                JSONObject returnobj = returns.getJSONObject(inx);
                String type = returnobj.getString("type");

                if (type.equals(".POGOProtos.Networking.Responses.FortDetailsResponse"))
                {
                    JSONObject data = returnobj.getJSONObject("data");

                    double latloc = data.getDouble("latitude@double");
                    double lonloc = data.getDouble("longitude@double");

                    Log.d(LOGTAG, "Fort encountered: lat=" + latloc + " lon=" + lonloc);

                    lat = latloc;
                    lon = lonloc;
                    latMove = 0;
                    lonMove = 0;

                    setCommand(0);
                }
            }
        }
        catch (Exception ex)
        {

        }
    }

    public static void testDat()
    {
        File extdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File extfile = new File(extdir, "pm.20160723.082357.json");

        byte[] data = Simple.readBinaryFile(extfile);
        Log.d(LOGTAG, "testDat: size=" + data.length);

        int von = -1;
        int bis = -1;
        int eof = -1;

        for (int inx = 4; inx < data.length; inx++)
        {
            if ((data[ inx - 4 ] == (int) '\n') && (data[ inx - 3 ] == (int) '-') && (data[ inx - 2 ] == (int) '-') && (data[ inx - 1 ] == (int) '\n'))
            {
                if (von < 0)
                {
                    von = inx;
                }
                else
                {
                    if (bis < 0)
                    {
                        bis = inx - 4;
                    }
                    else
                    {
                        if (eof < 0)
                        {
                            eof = inx - 4;
                        }
                    }
                }
            }
        }

        byte[] request = new byte[ bis - von ];

        System.arraycopy(data, von, request, 0, bis - von);

        bis += 4;

        byte[] response = new byte[ eof - bis ];

        System.arraycopy(data, bis, response, 0, eof - bis);

        PokemonDecode pd = new PokemonDecode();

        JSONObject result = pd.decode("test", request, response);

        Log.d(LOGTAG,"testDat: " + Json.toPretty(result));
    }
}
