package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.media.AudioManager;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.view.Gravity;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.nio.ByteBuffer;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;

import de.xavaro.android.common.ProtoBufferEncode;
import de.xavaro.android.common.PokemonImage;
import de.xavaro.android.common.PokemonProto;
import de.xavaro.android.common.PokemonDecode;

public class Pokemongo extends FrameLayout
{
    private static final String LOGTAG = "POKEDEZI";

    private static final int numPokemons = 151;
    private static final int freeMetersperSecond = 30;
    private static final int softBanSecondsPerKilometer = 50;

    private WindowManager.LayoutParams overlayParam;

    private final static String[] commandModeTexts = new String[]{ "STOP", "SEEK", "HUNT", "WAIT", "SPOT", "HOLD", "WALK" };

    private final static FrameLayout[] cmdButtons = new FrameLayout[ 3 ];
    private final static TextView[] cmdButtonTextViews = new TextView[ 3 ];
    private final static ImageView[] cmdButtonImageViews = new ImageView[ 3 ];
    private final static String[] cmdTexts = new String[]{ "HIDE", "SHOP", "HUNT" };

    private final static FrameLayout[] dirButtons = new FrameLayout[ 9 ];
    private final static TextView[] dirButtonTextViews = new TextView[ 9 ];
    private final static TextView[] dirButtonInfoViews = new TextView[ 9 ];
    private final static String[] dirTexts = new String[]{ "NW", "N", "NE", "W", "STOP", "E", "SW", "S", "SE" };
    private final static int[] dirMoveX = new int[]{ -1, 0, 1, -1, 0, 1, -1, 0, 1 };
    private final static int[] dirMoveY = new int[]{ 1, 1, 1, 0, 0, 0, -1, -1, -1 };
    private final static int[] dirNextDir = new int[]{ 7, 8, 3, 2, 4, 6, 5, 0, 1 };

    private final static FrameLayout[] spawns = new FrameLayout[ 27 ];
    private final static ImageView[] pimages = new ImageView[ 27 ];
    private final static TextView[] timages = new TextView[ 27 ];
    private final static JSONObject[] locsJson = new JSONObject[ 27 ];

    private final static int buttsize = 56;
    private final static int buttpad = 4;
    private final static int buttnetto = buttsize - buttpad * 2;

    private final static int xsize = buttsize * ((cmdButtons.length + dirButtons.length + spawns.length) / 3);
    private final static int ysize = buttsize * 3;

    private final static JSONObject pokeLocs = new JSONObject();

    private static FrameLayout pokeDir;
    private static FrameLayout[] pokeDirFrames;
    private static ImageView[] pokeDirImages;
    private static TextView[] pokeDirCounts;
    private static boolean[] pokeDirEnabled;
    private static boolean[] pokeDirHunting;
    private static int pokeDirCols;
    private static int pokeDirRows;

    private static JSONObject poke2spawn = new JSONObject();

    @SuppressLint("RtlHardcoded")
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

            lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;

            dirButtonInfoViews[ inx ] = new TextView(getContext());
            dirButtonInfoViews[ inx ].setTypeface(null, Typeface.BOLD);
            dirButtonInfoViews[ inx ].setTextSize(buttnetto / 3.0f);
            dirButtonInfoViews[ inx ].setTextColor(0xffffffff);
            dirButtonInfoViews[ inx ].setPadding(0, 0, 2, 0);
            dirButtonInfoViews[ inx ].setLayoutParams(lp);

            dirButtons[ inx ].addView(dirButtonInfoViews[ inx ]);
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

            lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;

            timages[ inx ] = new TextView(getContext());
            timages[ inx ].setTypeface(null, Typeface.BOLD);
            timages[ inx ].setTextSize(buttnetto / 3.0f);
            timages[ inx ].setTextColor(0xffff0000);
            timages[ inx ].setPadding(0, 0, 2, 0);
            timages[ inx ].setLayoutParams(lp);

            spawns[ inx ].addView(timages[ inx ]);

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
        pokeDirCounts = new TextView[ numPokemons ];

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

            lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;

            pokeDirCounts[ inx ] = new TextView(getContext());
            pokeDirCounts[ inx ].setTypeface(null, Typeface.BOLD);
            pokeDirCounts[ inx ].setTextSize(buttnetto / 3.0f);
            pokeDirCounts[ inx ].setTextColor(0xffff0000);
            pokeDirCounts[ inx ].setPadding(0, 0, 2, 0);
            pokeDirCounts[ inx ].setLayoutParams(lp);

            pokeDirFrames[ inx ].addView(pokeDirCounts[ inx ]);
        }

        isUpdatedir = true;
    }

    private static void updatePokemonDir()
    {
        if (! isUpdatedir) return;

        try
        {
            Iterator<String> keysIterator = poke2spawn.keys();

            while (keysIterator.hasNext())
            {
                String pokeId = keysIterator.next();

                String[] parts = pokeId.split("@");
                if (parts.length != 2) continue;

                int pokeOrd = Integer.parseInt(parts[ 1 ], 10);
                JSONArray pokeSpwans = poke2spawn.getJSONArray(pokeId);
                String count = "" + pokeSpwans.length();

                pokeDirEnabled[ pokeOrd - 1 ] = true;

                if (pokeDirHunting[ pokeOrd - 1 ])
                {
                    pokeDirImages[ pokeOrd - 1 ].setBackgroundColor(0xffffcccc);
                }
                else
                {
                    pokeDirImages[ pokeOrd - 1 ].setBackgroundColor(0xcccccccc);
                }

                pokeDirCounts[ pokeOrd - 1 ].setText(count);
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }

        isUpdatedir = false;
    }

    private static void buildPokeHuntSpawns()
    {
        synchronized (huntPointsTodo)
        {
            try
            {
                huntPointsTodo.clear();

                ArrayList<JSONObject> prep = new ArrayList<>();
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
                        JSONObject clone = new JSONObject(spawns.getJSONObject(inx).toString());
                        clone.put("pid", pokeId);
                        prep.add(clone);

                        if ((spawns.length() == 25) && (inx >= 8))
                        {
                            if (!pokeId.equals("PIKACHU@25")) break;
                        }
                    }
                }

                //
                // Shuffle list.
                //

                while (prep.size() > 0)
                {
                    int shuffle = (int) (Math.random() * prep.size());

                    Log.d(LOGTAG, "buildPokeHuntSpawns shuffle=" + shuffle);

                    huntPointsTodo.add(prep.remove(shuffle));
                }

                for (int inx = 0; inx < huntPointsTodo.size(); inx++)
                {
                    JSONObject item = huntPointsTodo.get(inx);

                    Log.d(LOGTAG,"buildPokeHuntSpawns list pid=" + item.getString("pid"));
                }

                Log.d(LOGTAG, "buildPokeHuntSpawns: size=" + huntPointsTodo.size());
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }
        }
    }

    private void onClickHuntPokemonButton(int buttinx)
    {
        if (!pokeDirEnabled[ buttinx ]) return;

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

    @SuppressLint("RtlHardcoded")
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
                if (!locsIterator.hasNext()) return;

                String latlonstr = locsIterator.next();
                JSONObject latlon = new JSONObject(latlonstr);
                long expires = locs.getLong(latlonstr);

                //
                // Reorder to back.
                //

                locs.remove(latlonstr);
                locs.put(latlonstr, expires);

                pimages[ buttinx ].setBackgroundColor(0xcccccccc);

                latTogo = latlon.getDouble("lat");
                lonTogo = latlon.getDouble("lon");

                isSpotting = true;
                isMoving = true;
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }
        }
    }

    private static int commandMode;
    private static long suspendTime;

    private static boolean isWalking;
    private static boolean isHolding;
    private static boolean isWaiting;
    private static boolean isSpotting;
    private static boolean isImportant;
    private static boolean isShowhunt;
    private static boolean isUpdatedir;

    private static void updateCommandStatus()
    {
        //
        // Derive walking button from walking parameters.
        //

        int currentButton = 4;

        int currentSpeed = (int) Math.round(Math.max(
                Math.abs(latWalk / (meterLat * 4)),
                Math.abs(lonWalk / (meterLon * 4))));

        int dirX = (lonWalk < 0) ? -1 : (lonWalk > 0) ? 1 : 0;
        int dirY = (latWalk < 0) ? -1 : (latWalk > 0) ? 1 : 0;

        for (int inx = 0; inx < dirMoveX.length; inx++)
        {
            if ((dirX == dirMoveX[ inx ]) && (dirY == dirMoveY[ inx ]))
            {
                currentButton = inx;
                break;
            }
        }

        if (currentButton != 4) isWalking = true;

        //
        // Higlight corresponding button.
        //

        for (int inx = 0; inx < dirButtonTextViews.length; inx++)
        {
            if (inx == currentButton)
            {
                dirButtonTextViews[ inx ].setTypeface(null, Typeface.BOLD);
                dirButtonTextViews[ inx ].setBackgroundColor(0xff008800);

                if (inx == 4)
                {
                    if (lastSec > 0)
                    {
                        String btext = "" + lastSec;
                        dirButtonInfoViews[ inx ].setText(btext);
                        dirButtonTextViews[ inx ].setBackgroundColor(0xffff0000);
                    }
                    else
                    {
                        dirButtonInfoViews[ inx ].setText(null);
                        dirButtonTextViews[ inx ].setBackgroundColor(0x88008800);
                    }

                }
                else
                {
                    String speed = "x" + currentSpeed;
                    dirButtonInfoViews[ inx ].setText(speed);
                }
            }
            else
            {
                dirButtonTextViews[ inx ].setTypeface(null, Typeface.NORMAL);
                dirButtonTextViews[ inx ].setBackgroundColor(0x88008800);
                dirButtonInfoViews[ inx ].setText(null);
            }
        }

        int cmd = commandMode;

        if (isSpotting) cmd = COMMAND_SPOT;
        if (isWalking) cmd = COMMAND_WALK;
        if (isWaiting) cmd = COMMAND_WAIT;
        if (isHolding) cmd = COMMAND_HOLD;

        if ((cmd != commandMode) && (lastSec <= 0))
        {
            dirButtonTextViews[ 4 ].setBackgroundColor(0xffcccc00);
        }

        dirButtonTextViews[ 4 ].setText(commandModeTexts[ cmd ]);
    }

    private void onClickJoystickButton(int buttinx)
    {
        if (buttinx == 4)
        {
            onClickMiddleButton();
        }
        else
        {
            onClickDirectionButton(buttinx);
        }

        updateCommandStatus();
    }

    private void onClickMiddleButton()
    {
        latWalk = 0.0;
        lonWalk = 0.0;
        latTogo = lat;
        lonTogo = lon;

        suspendTime = 0;

        if (isHolding || isWaiting || isSpotting || isWalking)
        {
            isHolding = false;
            isWaiting = false;
            isWalking = false;
            isSpotting = false;

            commandMode = COMMAND_STOP;
        }
        else
        {
            commandMode = ++commandMode % (COMMAND_HUNT + 1);
        }
    }

    private void onClickDirectionButton(int buttinx)
    {
        if (dirMoveY[ buttinx ] == 0) latWalk = 0;
        if (dirMoveX[ buttinx ] == 0) lonWalk = 0;

        lonWalk += dirMoveX[ buttinx ] * meterLon * 4;
        latWalk += dirMoveY[ buttinx ] * meterLat * 4;

        if (Math.abs(latWalk) < 0.00000001) latWalk = 0;
        if (Math.abs(lonWalk) < 0.00000001) lonWalk = 0;

        isMoving = false;
        isWaiting = false;
        isHolding = false;
        isWalking = false;
        isSpotting = false;

        suspendTime = 0;

        Log.d(LOGTAG, "onClickDirectionButton:"
                + " buttinx=" + buttinx
                + " latWalk=" + String.format(Locale.ROOT, "%.6f", latWalk)
                + " lonWalk=" + String.format(Locale.ROOT, "%.6f", lonWalk));
    }

    private static Pokemongo instance;
    private static Application application;

    private static void initPokemongo(Location location)
    {
        if (instance != null) return;

        if (lat == 0) lat = location.getLatitude();
        if (lon == 0) lon = location.getLongitude();

        loadPosition();

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

    private void sampleCalls(Location location)
    {
        deziLocation(location);

        String url = "url";
        int offset = 1;
        int size = 2;
        byte[] data = new byte[ 1 ];
        ByteBuffer buffer = ByteBuffer.wrap(data);

        pokeWriteText(url);
        pokeOpenFile(url);
        pokeWriteBytes(url, data, offset, size);
        pokeWriteBuffer(url, buffer, offset, size);
        pokeCloseFile(url);
    }

    //
    // Trittau => 53.612295, 10.393479
    //

    //private static String region = "de.Trittau";
    //private static double latRegion = 53.61;
    //private static double lonRegion = 10.39;

    //
    // Hamburg => 53.55, 10.00
    //

    //private static String region = "de.Hamburg";
    //private static double latRegion = 53.55;
    //private static double lonRegion = 10.00;

    //
    // Köln => 50.946695, 6.971266
    //

    //private static String region = "de.Köln";
    //private static double latRegion = 50.94;
    //private static double lonRegion =  6.97;

    //
    // Berlin => 52.511389, 13.411379
    //

    //private static String region = "de.Berlin";
    //private static double latRegion = 52.51;
    //private static double lonRegion = 13.41;

    //
    // Tokyo => 35.652442, 139.757809
    //

    //private static String region = "jp.Tokyo";
    //private static double latRegion =  35.65;
    //private static double lonRegion = 139.75;

    //
    // New York => 40.705161, -74.013142
    //

    //private static String region = "us.NewYork";
    //private static double latRegion =  40.70;
    //private static double lonRegion = -74.01;

    //
    // Sydney => -33.872806, 151.207502
    //

    private static String region = "au.Sydney";
    private static double latRegion = -33.87;
    private static double lonRegion = 151.20;

    private static double lat = latRegion; // + ((Math.random() - 0.5) / 50.0);
    private static double lon = lonRegion; // + ((Math.random() - 0.5) / 50.0);

    private static double latWalk = 0;
    private static double lonWalk = 0;

    private static double latMin = +1000;
    private static double latMax = -1000;
    private static double lonMin = +1000;
    private static double lonMax = -1000;

    private static final int COMMAND_STOP = 0;
    private static final int COMMAND_SEEK = 1;
    private static final int COMMAND_HUNT = 2;

    private static final int COMMAND_WAIT = 3;
    private static final int COMMAND_SPOT = 4;
    private static final int COMMAND_HOLD = 5;
    private static final int COMMAND_WALK = 6;

    private static boolean isMoving;
    private static double latTogo = lat;
    private static double lonTogo = lon;

    public static void deziLocation(Location location)
    {
        Log.d(LOGTAG, "pupsekacke in...");

        try
        {
            initPokemongo(location);

            updatePokemonDir();

            isWaiting = (suspendTime >= new Date().getTime());

            updateCommandStatus();

            if (! (isHolding || isWaiting))
            {
                if (! (isMoving || isWalking || isSpotting))
                {
                    if (commandMode == COMMAND_SEEK) doSeekCommand();
                    if (commandMode == COMMAND_HUNT) doHuntCommand();
                }

                if (isMoving)
                {
                    if ((Math.abs(lat - latTogo) > 0.0001) || (Math.abs(lon - lonTogo) > 0.0001))
                    {
                        double latDist = (latTogo - lat) / 2.0;
                        double lonDist = (lonTogo - lon) / 2.0;

                        if (latDist > +0.0015) latDist = +0.0015;
                        if (lonDist > +0.0015) lonDist = +0.0015;
                        if (latDist < -0.0015) latDist = -0.0015;
                        if (lonDist < -0.0015) lonDist = -0.0015;

                        lat += latDist;
                        lon += lonDist;

                        Log.d(LOGTAG, "deziLocation: goto lat=" + lat + " lon=" + lon);
                        Log.d(LOGTAG, "deziLocation: dist lat=" + latTogo + " lon=" + lonTogo);
                    }
                    else
                    {
                        isMoving = false;

                        if (commandMode == COMMAND_SEEK)
                        {
                            suspendTime = new Date().getTime() + 2 * 1000;
                        }

                        if (commandMode == COMMAND_HUNT)
                        {
                            suspendTime = new Date().getTime() + 15 * 1000;
                        }
                    }
                }

                if (isWalking)
                {
                    lat += latWalk;
                    lon += lonWalk;

                    if ((lat < latMin) || (lat > latMax)) latWalk = -latWalk;
                    if ((lon < lonMin) || (lon > lonMax)) lonWalk = -lonWalk;
                }

                if (isSpotting)
                {
                    if (!isMoving)
                    {
                        isSpotting = false;

                        suspendTime = new Date().getTime() + (isImportant ? 120 : 18) * 1000;
                    }
                }
            }

            setPosition(location);

            setupSpawns();

            makeToast();
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }

        System.gc();

        Log.d(LOGTAG, "pupsekacke out...");
    }

    private static void doSeekCommand()
    {
        try
        {
            if (spawnPointsTodo.size() > 0)
            {
                synchronized (spawnPointsTodo)
                {
                    String spanposstr = spawnPointsTodo.remove(spawnPointsTodo.size() - 1);
                    spawnPointsSeen.add(spanposstr);

                    JSONObject spanpos = new JSONObject(spanposstr);

                    lat = spanpos.getDouble("lat");
                    lon = spanpos.getDouble("lon");

                    suspendTime = new Date().getTime() + 2 * 1000;
                }

                Log.d(LOGTAG, "deziLocation: seek lat=" + lat + " lon=" + lon);
            }
            else
            {
                synchronized (spawnPointsTodo)
                {
                    spawnPointsSeen.clear();
                }
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    private static void doHuntCommand()
    {
        if (huntPointsTodo.size() == 0) buildPokeHuntSpawns();

        if (huntPointsTodo.size() > 0)
        {
            try
            {
                String pid = null;
                int ord = 0;

                synchronized (huntPointsTodo)
                {
                    JSONObject huntPoint = getNearestPoint(huntPointsTodo);

                    if (huntPoint != null)
                    {
                        pid = huntPoint.getString("pid");

                        //latTogo = huntPoint.getDouble("lat");
                        //lonTogo = huntPoint.getDouble("lon");
                        //isMoving = true;

                        lat = huntPoint.getDouble("lat");
                        lon = huntPoint.getDouble("lon");

                        suspendTime = new Date().getTime() + 2 * 1000;
                    }
                }

                String counttext = "" + huntPointsTodo.size();
                timages[ pimages.length - 1 ].setText(counttext);

                if (pid != null)
                {
                    String[] parts = pid.split("@");

                    if (parts.length == 2)
                    {
                        ord = Integer.parseInt(parts[ 1 ], 10);

                        pimages[ pimages.length - 1 ].setImageDrawable(pokeDirImages[ ord - 1 ].getDrawable());
                    }
                }

                Log.d(LOGTAG, "deziLocation: hunt"
                        + " lat=" + lat + " lon=" + lon
                        + " pid=" + pid + " ord=" + ord);
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }
        }
    }

    @Nullable
    private static JSONObject getNearestPoint(ArrayList<JSONObject> spanLocations)
    {
        try
        {
            int bestInx = -1;
            double bestDist = 0.0;

            for (int inx = 0; inx < spanLocations.size(); inx++)
            {
                JSONObject spanLocation = spanLocations.get(inx);

                double distLat = lat - spanLocation.getDouble("lat");
                double distLon = lon - spanLocation.getDouble("lon");

                double dist = Math.sqrt(distLat * distLat + distLon * distLon);

                if (dist < 0.001)
                {
                    //
                    // Span location is almost the same as actual.
                    //

                    spanLocations.remove(inx--);
                    continue;
                }

                if ((bestInx < 0) || (dist < bestDist))
                {
                    bestInx = inx;
                    bestDist = dist;
                }
            }

            Log.d(LOGTAG, "getNearestPoint: inx=" + bestInx);

            return (bestInx >= 0) ? spanLocations.remove(bestInx) : null;
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();

            return null;
        }
    }

    private static final Map<Integer, OutputStream> outputs = new HashMap<>();
    private static final Map<Integer, byte[]> postdata = new HashMap<>();
    private static final ArrayList<File> logfiles = new ArrayList<>();

    private static final ArrayList<JSONObject> huntPointsTodo = new ArrayList<>();
    private static final ArrayList<String> spawnPointsTodo = new ArrayList<>();
    private static final ArrayList<String> spawnPointsSeen = new ArrayList<>();

    // http://www.pokemon.com/de/api/pokedex/kalos
    // http://www.pok
    // emon.com/us/api/pokedex/kalos
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
        String filename = "Request." + df.format(new Date()) + ".json";

        File extdir = Environment.getExternalStorageDirectory();
        File extpoke = new File(extdir, "Mongopoke");
        if (!(extpoke.exists() || extpoke.mkdir())) return;
        File extsubdir = new File(extpoke, "Requests");
        if (!(extsubdir.exists() || extsubdir.mkdir())) return;

        File extfile = new File(extsubdir, filename);

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

        Log.d(LOGTAG, "pokeWriteBytes: offset=" + offset + " len=" + count + " hash=" + urlhash);

        OutputStream out = outputs.get(urlhash);
        byte[] request = postdata.get(urlhash);

        if (out != null)
        {
            try
            {
                Log.d(LOGTAG, "pokeWriteBytes: found... offset=" + offset + " len=" + count + " hash=" + urlhash);

                //out.write(buffer, offset, count);

                boolean initial = (request.length == 0);

                byte[] newdata = new byte[ request.length + (count - offset) ];
                System.arraycopy(request, 0, newdata, 0, request.length);
                System.arraycopy(buffer, offset, newdata, request.length, count - offset);

                if (initial && (newdata.length < 32765))
                {
                    PokemonDecode dc = new PokemonDecode();
                    JSONObject requestEnv = dc.decodeRequest(newdata);

                    ArrayList<String> messages = dc.patchRequest(requestEnv, newdata);
                    System.arraycopy(newdata, request.length, buffer, offset, count - offset);

                    if (messages != null)
                    {
                        while (messages.size() > 0)
                        {
                            addToast(messages.remove(0));
                        }
                    }
                }

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

        Log.d(LOGTAG, "pokeWriteBuffer: offset=" + offset + " len=" + count + " hash=" + urlhash);

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

                    PokemonDecode pd = new PokemonDecode();
                    JSONObject result = pd.decode(url, request, response);

                    if (result != null)
                    {
                        String jres = result.toString(2);
                        out.write(jres.replace("\\/", "/").getBytes());

                        saveRecords(result);

                        evalMapDetails(result);
                        evalGymDetails(result);
                        evalFortDetails(result);
                        evalItemCapture(result);
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
        File extdir = Environment.getExternalStorageDirectory();
        File extpoke = new File(extdir, "Mongopoke");
        File extsamples = new File(extpoke, "Samples");

        if (!(extsamples.exists() || extsamples.mkdirs())) return;

        try
        {
            JSONObject rpcrequest = json.getJSONObject("request");
            JSONArray requests = rpcrequest.getJSONArray("requests@.POGOProtos.Networking.Requests.Request");

            for (int rinx = 0; rinx < requests.length(); rinx++)
            {
                JSONObject request = requests.getJSONObject(rinx);

                String restype = request.getString("request_type@.POGOProtos.Networking.Requests.RequestType");

                Log.d(LOGTAG, "saveRecords: req type=" + restype);

                String file = "Samples.Requests." + CamelName(restype) + "Response.json";
                File extfile = new File(extsamples, file);

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
                String file = "Samples." + parts[ parts.length - 2 ] + "." + parts[ parts.length - 1 ] + ".json";
                File extfile = new File(extsamples, file);

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
                File extspawn = new File(extpoke, "Harvest.SpawnMap." + region + ".json");

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
                        JSONArray pokeSpwans = poke2spawn.getJSONArray(pokeId);

                        while (pokeSpwans.length() > 25) pokeSpwans.remove(0);

                        for (int inx = 0; inx < pokeSpwans.length(); inx++)
                        {
                            JSONObject pokeSpwan = pokeSpwans.getJSONObject(inx);

                            double slat = pokeSpwan.getDouble("lat");
                            double slon = pokeSpwan.getDouble("lon");

                            if (slat < latMin) latMin = slat;
                            if (slat > latMax) latMax = slat;
                            if (slon < lonMin) lonMin = slon;
                            if (slon > lonMax) lonMax = slon;

                            /*
                            if (pokeId.equals("PIKACHU@25"))
                            {
                                JSONObject clone = new JSONObject(pokeSpwan.toString());
                                clone.put("pid", pokeId);
                                huntPointsTodo.add(clone);
                            }
                            */
                        }
                    }
                }

                Log.d(LOGTAG, "loadPokeSpawnMap:"
                        + " latMin=" + latMin + " latMax=" + latMax
                        + " lonMin=" + lonMin + " lonMax=" + lonMax);
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
                File extspawn = new File(extpoke, "Harvest.SpawnMap." + region + ".json");

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

            if (!poke2spawn.has(pokeId)) poke2spawn.put(pokeId, new JSONArray());
            JSONArray spawns = poke2spawn.getJSONArray(pokeId);

            if (spawns.length() >= 25) return;

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

            isUpdatedir = true;

            addToast("New Spawn " + pokeId);
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

                tthidden = 120 * 1000;
            }

            String[] parts = pokeId.split("@");
            if (parts.length != 2) return;
            int pokeOrd = Integer.parseInt(parts[ 1 ], 10);

            if (commandMode == COMMAND_HUNT)
            {
                if (! pokeDirEnabled[ pokeOrd - 1 ])
                {
                    //
                    // Pokemon is new. Auto enable and hunt.
                    //

                    pokeDirHunting[ pokeOrd - 1 ] = true;

                    savePokeHuntSettings();
                }

                if (! pokeDirHunting[ pokeOrd - 1 ])
                {
                    Log.d(LOGTAG, "addPokepos: see tag=" + tag + " id=" + pokeId + " tth=" + (tthidden / 1000) + " chk=" + pokeposstr);

                    return;
                }
            }

            isImportant = false;

            if (commandMode == COMMAND_SEEK)
            {
                if (pokeDirHunting[ pokeOrd - 1 ])
                {
                    AudioManager am = (AudioManager) application.getSystemService(Context.AUDIO_SERVICE);
                    am.playSoundEffect(SoundEffectConstants.CLICK);

                    isImportant = true;
                }
            }

            synchronized (pokeLocs)
            {
                if (!pokeLocs.has(pokeId)) pokeLocs.put(pokeId, new JSONObject());
                JSONObject pokeLoc = pokeLocs.getJSONObject(pokeId);

                pokeLoc.put("pid", pokeId);
                pokeLoc.put("ord", pokeOrd);

                if (!pokeLoc.has("loc")) pokeLoc.put("loc", new JSONObject());
                JSONObject loc = pokeLoc.getJSONObject("loc");

                if (! loc.has(pokeposstr))
                {
                    long expires = new Date().getTime() + tthidden;
                    loc.put(pokeposstr, expires);

                    if (isImportant || ((commandMode == COMMAND_HUNT) && !isSpotting))
                    {
                        //
                        // Goto pokemon spot.
                        //

                        latTogo = plat;
                        lonTogo = plon;

                        isMoving = true;
                        isSpotting = true;

                        suspendTime = 0;
                    }
                }
            }

            Log.d(LOGTAG, "addPokepos: add tag=" + tag + " id=" + pokeId + " tth=" + (tthidden / 1000) + " chk=" + pokeposstr);
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
            double spawnLat = spawn.getDouble("latitude@double");
            double spawnLon = spawn.getDouble("longitude@double");

            JSONObject spawnpos = new JSONObject();
            spawnpos.put("lat", spawnLat);
            spawnpos.put("lon", spawnLon);

            String spawnposstr = spawnpos.toString();

            synchronized (spawnPointsTodo)
            {
                if (! (spawnPointsTodo.contains(spawnposstr) || spawnPointsSeen.contains(spawnposstr)))
                {
                    spawnPointsTodo.add(spawnposstr);

                    while (spawnPointsTodo.size() > 1000)
                    {
                        spawnPointsTodo.remove(0);
                    }
                }
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
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

                            if (spawns.length() > 0)
                            {
                                JSONObject spawn = spawns.getJSONObject(0);
                                registerSpawn(spawn);
                                haveSpawn = true;
                            }
                        }

                        if ((!haveSpawn) && mapcell.has("decimated_spawn_points@.POGOProtos.Map.SpawnPoint"))
                        {
                            JSONArray spawns = mapcell.getJSONArray("decimated_spawn_points@.POGOProtos.Map.SpawnPoint");

                            if (spawns.length() > 0)
                            {
                                JSONObject spawn = spawns.getJSONObject(0);
                                registerSpawn(spawn);
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

    public static void clearSpawnPoint(double latPos, double lonPos)
    {
        synchronized (pokeLocs)
        {
            try
            {
                Log.d(LOGTAG, "clearSpawnPoint lat=" + latPos + " lon=" + lonPos);

                JSONObject latlon = new JSONObject();
                latlon.put("lat", latPos);
                latlon.put("lon", lonPos);
                String latlonStr = latlon.toString();

                Iterator<String> keysIterator = pokeLocs.keys();
                ArrayList<String> removePokes = new ArrayList<>();

                while (keysIterator.hasNext())
                {
                    String pokeId = keysIterator.next();

                    JSONObject pokeLoc = pokeLocs.getJSONObject(pokeId);
                    JSONObject locs = pokeLoc.getJSONObject("loc");

                    Iterator<String> locsIterator = locs.keys();
                    ArrayList<String> removeLocs = new ArrayList<>();

                    int valid = 0;

                    while (locsIterator.hasNext())
                    {
                        if (latlonStr.equals(locsIterator.next()))
                        {
                            removeLocs.add(latlonStr);

                            Log.d(LOGTAG, "clearSpawnPoint pid=" + pokeId);
                        }
                        else
                        {
                            valid++;
                        }
                    }

                    while (removeLocs.size() > 0) locs.remove(removeLocs.remove(0));

                    if (valid == 0) removePokes.add(pokeId);
                }

                while (removePokes.size() > 0) pokeLocs.remove(removePokes.remove(0));
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }
        }

    }

    public static void setupSpawns()
    {
        synchronized (pokeLocs)
        {
            //
            // Remove expired spawns.
            //

            try
            {
                long now = new Date().getTime();

                Iterator<String> keysIterator = pokeLocs.keys();
                ArrayList<String> removePokes = new ArrayList<>();

                while (keysIterator.hasNext())
                {
                    String pokeId = keysIterator.next();

                    JSONObject pokeLoc = pokeLocs.getJSONObject(pokeId);
                    JSONObject locs = pokeLoc.getJSONObject("loc");

                    Iterator<String> locsIterator = locs.keys();
                    ArrayList<String> removeLocs = new ArrayList<>();

                    int valid = 0;

                    while (locsIterator.hasNext())
                    {
                        String latlonstr = locsIterator.next();
                        long expiration = locs.getLong(latlonstr);

                        if (expiration < now)
                        {
                            removeLocs.add(latlonstr);
                        }
                        else
                        {
                            valid++;
                        }
                    }

                    while (removeLocs.size() > 0) locs.remove(removeLocs.remove(0));

                    if (valid == 0) removePokes.add(pokeId);
                }

                while (removePokes.size() > 0) pokeLocs.remove(removePokes.remove(0));
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
                    int count = countJSONObjects(pokeLoc.getJSONObject("loc"));

                    String counttext = "" + count;
                    timages[ spawninx ].setText(counttext);

                    if (locsJson[ spawninx ] != pokeLoc)
                    {
                        locsJson[ spawninx ] = pokeLoc;

                        int pokeOrd = pokeLoc.getInt("ord");

                        pimages[ spawninx ].setImageDrawable(pokeDirImages[ pokeOrd - 1 ].getDrawable());
                    }

                    pimages[ spawninx ].setBackgroundColor(0xffffffff);

                    spawninx++;
                }

                int max = spawns.length - ((commandMode == COMMAND_HUNT) ? 1 : 0);

                while (spawninx < max)
                {
                    locsJson[ spawninx ] = null;

                    pimages[ spawninx ].setImageDrawable(null);
                    pimages[ spawninx ].setBackgroundColor(0xffffffff);
                    timages[ spawninx ].setText(null);

                    spawninx++;
                }
            }
            catch (Exception ignore)
            {
                ignore.printStackTrace();
            }
        }
    }

    private static double latEncounter;
    private static double lonEncounter;

    private static void evalItemCapture(JSONObject json)
    {
        try
        {
            JSONObject response = json.getJSONObject("response");
            JSONArray returns = response.getJSONArray("returns@array");

            for (int inx = 0; inx < returns.length(); inx++)
            {
                JSONObject returnobj = returns.getJSONObject(inx);
                String type = returnobj.getString("type");
                JSONObject data = returnobj.getJSONObject("data");

                if (type.equals(".POGOProtos.Networking.Responses.EncounterResponse"))
                {
                    Log.d(LOGTAG, "evalItemCapture: encounter json=" + data.toString(2));

                    JSONObject wildPoke = data.getJSONObject("wild_pokemon@.POGOProtos.Map.Pokemon.WildPokemon");
                    latEncounter = wildPoke.getDouble("latitude@double");
                    lonEncounter = wildPoke.getDouble("longitude@double");

                    Log.d(LOGTAG, "evalItemCapture: encounter lat=" + latEncounter + " lon=" + lonEncounter);

                    data = data.getJSONObject("capture_probability@.POGOProtos.Data.Capture.CaptureProbability");

                    String text = "";

                    if (data.has("pokeball_type@.POGOProtos.Inventory.ItemId") && data.has("capture_probability@float"))
                    {
                        JSONArray items = data.getJSONArray("pokeball_type@.POGOProtos.Inventory.ItemId");
                        if (items.get(0) instanceof JSONArray) items = items.getJSONArray(0);

                        JSONArray probas = data.getJSONArray("capture_probability@float");
                        if (probas.get(0) instanceof JSONArray) probas = probas.getJSONArray(0);

                        if (items.length() == probas.length())
                        {
                            for (int cnt = 0; cnt < items.length(); cnt++)
                            {
                                String item = items.getString(cnt);
                                double proba = probas.getDouble(cnt);

                                if (item.equals("ITEM_POKE_BALL@1")) item = "Normal Ball";
                                if (item.equals("ITEM_GREAT_BALL@2")) item = "Great Ball";
                                if (item.equals("ITEM_ULTRA_BALL@3")) item = "Ultra Ball";
                                if (item.equals("ITEM_MASTER_BALL@4")) item = "Master Ball";

                                text += item + " = " + proba + "\n";
                            }
                        }
                    }

                    text = text.trim();

                    if (text.isEmpty()) text = "No Encounter Data";

                    Log.d(LOGTAG, "evalItemCapture: encounter text=" + text);

                    addToast(text);

                    isHolding = true;
                }

                if (type.equals(".POGOProtos.Networking.Responses.UseItemCaptureResponse"))
                {
                    Log.d(LOGTAG, "evalItemCapture: capture json=" + data.toString(2));

                    String text = "";

                    if (data.has("item_capture_mult@double"))
                    {
                        double mult = data.getDouble("item_capture_mult@double");

                        text += " Capture x " + mult;
                    }

                    if (data.has("item_flee_mult@double"))
                    {
                        double mult = data.getDouble("item_flee_mult@double");

                        text += " Flee x " + mult;
                    }

                    text = text.trim();

                    if (text.isEmpty()) text = "No effect";

                    Log.d(LOGTAG, "evalItemCapture: capture text=" + text);

                    addToast(text);
                }

                if (type.equals(".POGOProtos.Networking.Responses.CatchPokemonResponse"))
                {
                    Log.d(LOGTAG, "evalItemCapture: catch json=" + data.toString(2));

                    if (data.has("status@.POGOProtos.Networking.Responses.CatchPokemonResponse.CatchStatus"))
                    {
                        String status = data.getString("status@.POGOProtos.Networking.Responses.CatchPokemonResponse.CatchStatus");

                        //
                        // CATCH_ERROR = 0;
                        // CATCH_SUCCESS = 1;
                        // CATCH_ESCAPE = 2;
                        // CATCH_FLEE = 3;
                        // CATCH_MISSED = 4;
                        //

                        Log.d(LOGTAG, "evalItemCapture catch=" + status);

                        if (status.equals("CATCH_ERROR@0")
                                || status.equals("CATCH_SUCCESS@1")
                                || status.equals("CATCH_FLEE@3"))
                        {
                            isHolding = false;

                            clearSpawnPoint(latEncounter, lonEncounter);

                            addToast(status.equals("CATCH_SUCCESS@1") ? "GOT!" : "NIX!");
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

                    isHolding = true;
                }
            }
        }
        catch (Exception ignore)
        {
        }
    }

    public static void testEncode()
    {
        try
        {
            String text = "{\n" +
                    "  \"request_type@.POGOProtos.Networking.Requests.RequestType\": \"ENCOUNTER@102\",\n" +
                    "  \"request_message@bytes@\": 3,\n" +
                    "  \"request_message@.POGOProtos.Networking.Requests.Messages.EncounterMessage\": {\n" +
                    "    \"encounter_id@fixed64\": -2372265606374053763,\n" +
                    "    \"spawn_point_id@string\": \"47b18f1ab11\",\n" +
                    "    \"player_latitude@double\": 53.54851531982422,\n" +
                    "    \"player_longitude@double\": 9.985169410705566\n" +
                    "  }\n" +
                    "}";

            ProtoBufferEncode encode = new ProtoBufferEncode();
            encode.setProtos(PokemonProto.getProtos());

            Log.d(LOGTAG, "testEncode: " + text);

            JSONObject result = encode.encodeJSON(new JSONObject(text), ".POGOProtos.Networking.Requests.Request");
            Log.d(LOGTAG, "testEncode: " + ((result == null) ? "Fail" : result.toString(2)));

        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    //region Fort methods.

    private static void evalFortDetails(JSONObject json)
    {
        try
        {
            JSONObject responseEnv = json.getJSONObject("response");
            JSONArray returns = responseEnv.getJSONArray("returns@array");

            for (int inx = 0; inx < returns.length(); inx++)
            {
                JSONObject returnobj = returns.getJSONObject(inx);
                String type = returnobj.getString("type");

                if (type.equals(".POGOProtos.Networking.Responses.FortDetailsResponse"))
                {
                    JSONObject data = returnobj.getJSONObject("data");

                    double latloc = data.getDouble("latitude@double");
                    double lonloc = data.getDouble("longitude@double");

                    Log.d(LOGTAG, "evalFortDetails: encountered: lat=" + latloc + " lon=" + lonloc);

                    lat = latloc;
                    lon = lonloc;

                    isHolding = true;
                }

                if (type.equals(".POGOProtos.Networking.Responses.FortSearchResponse"))
                {
                    isHolding = false;
                }
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    //endregion Fort methods.

    //region Utility methods.

    private static Application getApplicationUsingReflection() throws Exception
    {
        return (Application) Class.forName("android.app.AppGlobals")
                .getMethod("getInitialApplication").invoke(null, (Object[]) null);
    }

    private static final ArrayList<String> toastMessages = new ArrayList<>();

    private static void addToast(String message)
    {
        try
        {
            synchronized (toastMessages)
            {
                toastMessages.add(message);
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    private static void makeToast()
    {
        try
        {
            String message;

            synchronized (toastMessages)
            {
                if (toastMessages.size() == 0) return;
                message = toastMessages.remove(0);
            }

            Toast toast = Toast.makeText(application, message, Toast.LENGTH_SHORT);
            TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
            if (view != null) view.setGravity(Gravity.CENTER);
            toast.show();
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
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
            ignore.printStackTrace();
        }

        return false;
    }

    private static int countJSONObjects(JSONObject json)
    {
        int count = 0;

        Iterator<String> keysIterator = json.keys();

        while (keysIterator.hasNext())
        {
            keysIterator.next();
            count++;
        }

        return count;
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

    //endregion Utility methods.

    //region Timing methods.

    private static double meterLat;
    private static double meterLon;

    private static long lastTim;
    private static long lastBan;
    private static long lastSec;

    private static double lastLat;
    private static double lastLon;

    private static void loadPosition()
    {
        try
        {
            File extdir = Environment.getExternalStorageDirectory();
            File extpoke = new File(extdir, "Mongopoke");

            if (extpoke.exists())
            {
                File extfile = new File(extpoke, "Settings.Position.json");

                FileInputStream input = new FileInputStream(extfile);
                int size = (int) input.getChannel().size();
                byte[] content = new byte[ size ];
                int xfer = input.read(content);
                input.close();

                if (size == xfer)
                {
                    JSONObject position = new JSONObject(new String(content));

                    lastTim = position.getLong("tim");
                    lastBan = position.getLong("ban");
                    lastLat = position.getDouble("lat");
                    lastLon = position.getDouble("lon");

                    lat = lastLat;
                    lon = lastLon;
                }
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    private static void savePosition()
    {
        try
        {
            File extdir = Environment.getExternalStorageDirectory();
            File extpoke = new File(extdir, "Mongopoke");

            if (extpoke.exists() || extpoke.mkdir())
            {
                JSONObject position = new JSONObject();

                position.put("tim", lastTim);
                position.put("ban", lastBan);
                position.put("lat", lastLat);
                position.put("lon", lastLon);

                File extfile = new File(extpoke, "Settings.Position.json");

                OutputStream out = new FileOutputStream(extfile);
                out.write(position.toString(2).replace("\\/", "/").getBytes());
                out.close();
            }
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    private static void setPosition(Location location)
    {
        meterPerDegree();

        long now = new Date().getTime();
        long use = (now - lastTim);

        double xMeter = (lastLat - lat) / meterLat;
        double yMeter = (lastLon - lon) / meterLon;

        double freeMeter = freeMetersperSecond * (use / 1000.0);
        double doneMeter = Math.sqrt(xMeter * xMeter + yMeter * yMeter);
        double banSeconds = ((doneMeter - freeMeter) / 1000.0) * softBanSecondsPerKilometer;

        if (banSeconds > 0)
        {
            long nextSoftBan = now + (int) (banSeconds * 1000.0);
            if (nextSoftBan > lastBan) lastBan = nextSoftBan;
        }

        lastSec = (lastBan - now) / 1000;
        if (lastSec < 0) lastSec = 0;
        if (lastSec > 7200) lastSec = 7200;

        location.setLatitude(lat);
        location.setLongitude(lon);

        Log.d(LOGTAG, "setPosition:"
                + " lat=" + location.getLatitude()
                + " lon=" + location.getLongitude()
                + " mps=" + ((int) (doneMeter / (use / 1000.0)))
                + " dst=" + ((int) doneMeter)
                + " ban=" + lastSec
        );

        lastLat = lat;
        lastLon = lon;
        lastTim = now;

        savePosition();
    }

    private static void meterPerDegree()
    {
        double mLan = 111132.954 - 559.822 * Math.cos(2 * lat) + 1.175 * Math.cos(4 * lat);
        double mLon = 111132.954 * Math.cos(lat);

        meterLat = 1.0 / Math.abs(mLan);
        meterLon = 1.0 / Math.abs(mLon);
    }

    //endregion Timing methods.
}
