package de.xavaro.android.common;

import android.webkit.JavascriptInterface;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class WebAppNine
{
    private static final String LOGTAG = WebAppNine.class.getSimpleName();

    private final WebAppLoader webapploader;

    public WebAppNine(WebAppLoader webapploader)
    {
        this.webapploader = webapploader;
    }

    @JavascriptInterface
    public String getNinePatchBase64(String src, int width, int height)
    {
        JSONObject result = new JSONObject();

        String base64 = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC"
                + "0lEQVR42mNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=";

        int[] hpad = new int[ 2 ];
        int[] vpad = new int[ 2 ];

        byte[] content = webapploader.getRequestData(src);

        if (content != null)
        {
            try
            {
                Bitmap nine = BitmapFactory.decodeByteArray(content, 0, content.length);

                int wid = nine.getWidth();
                int hei = nine.getHeight();

                int[] horz = new int[ 4 ];
                int[] vert = new int[ 4 ];

                horz[ 0 ] = 1;
                vert[ 0 ] = 1;
                horz[ 3 ] = wid - 1;
                vert[ 3 ] = hei - 1;

                for (int inx = 0; inx < wid; inx++)
                {
                    if (nine.getPixel(inx, 0) != 0)
                    {
                        if (horz[ 1 ] == 0) horz[ 1 ] = inx;
                    }
                    else
                    {
                        if ((horz[ 1 ] != 0) && (horz[ 2 ] == 0)) horz[ 2 ] = inx;
                    }

                    if (nine.getPixel(inx, hei - 1) != 0)
                    {
                        if (hpad[ 0 ] == 0) hpad[ 0 ] = inx - 1;
                    }
                    else
                    {
                        if ((hpad[ 0 ] != 0) && (hpad[ 1 ] == 0)) hpad[ 1 ] = wid - inx - 1;
                    }
                }

                for (int inx = 0; inx < hei; inx++)
                {
                    if (nine.getPixel(0, inx) != 0)
                    {
                        if (vert[ 1 ] == 0) vert[ 1 ] = inx;
                    }
                    else
                    {
                        if ((vert[ 1 ] != 0) && (vert[ 2 ] == 0)) vert[ 2 ] = inx;
                    }

                    if (nine.getPixel(wid - 1, inx) != 0)
                    {
                        if (vpad[ 0 ] == 0) vpad[ 0 ] = inx - 1;
                    }
                    else
                    {
                        if ((vpad[ 0 ] != 0) && (vpad[ 1 ] == 0)) vpad[ 1 ] = hei - inx - 1;
                    }
                }

                int[] hdst = new int[ 4 ];
                int[] vdst = new int[ 4 ];

                hdst[ 0 ] = 0;
                vdst[ 0 ] = 0;

                hdst[ 1 ] = horz[ 1 ] - horz[ 0 ];
                vdst[ 1 ] = vert[ 1 ] - vert[ 0 ];

                hdst[ 2 ] = width - (horz[ 3 ] - horz[ 2 ]);
                vdst[ 2 ] = height - (vert[ 3 ] - vert[ 2 ]);

                hdst[ 3 ] = width - 1;
                vdst[ 3 ] = height - 1;

                /*
                Log.d(LOGTAG, "size=" + wid + ":" + hei);
                Log.d(LOGTAG, "horz=" + horz[ 0 ] + ":" + horz[ 1 ] + ":" + horz[ 2 ] + ":" + horz[ 3 ]);
                Log.d(LOGTAG, "vert=" + vert[ 0 ] + ":" + vert[ 1 ] + ":" + vert[ 2 ] + ":" + vert[ 3 ]);
                Log.d(LOGTAG, "hdst=" + hdst[ 0 ] + ":" + hdst[ 1 ] + ":" + hdst[ 2 ] + ":" + hdst[ 3 ]);
                Log.d(LOGTAG, "vdst=" + vdst[ 0 ] + ":" + vdst[ 1 ] + ":" + vdst[ 2 ] + ":" + vdst[ 3 ]);
                Log.d(LOGTAG, "hpad=" + hpad[ 0 ] + ":" + hpad[ 1 ]);
                Log.d(LOGTAG, "vpad=" + vpad[ 0 ] + ":" + vpad[ 1 ]);
                */

                Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(output);
                Paint paint = new Paint();

                for (int hinx = 0; hinx < 3; hinx++)
                {
                    for (int vinx = 0; vinx < 3; vinx++)
                    {
                        Rect sRect = new Rect(
                                horz[ hinx ], vert[ vinx ],
                                horz[ hinx + 1 ], vert[ vinx + 1 ]);

                        Rect dRect = new Rect(
                                hdst[ hinx ], vdst[ vinx ],
                                hdst[ hinx + 1 ], vdst[ vinx + 1 ]);

                        canvas.drawBitmap(nine, sRect, dRect, paint);
                    }
                }

                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                output.compress(Bitmap.CompressFormat.PNG, 0, bout);

                byte[] ba = bout.toByteArray();
                base64 = "data:image/png;base64," + Base64.encodeToString(ba, 0, ba.length, 0);
            }
            catch (Exception ignore)
            {
                Log.d(LOGTAG, "getNinePatchBase64: corrupt:" + src);
            }
        }

        Json.put(result, "base64", base64);

        Json.put(result, "paddingLeft",   hpad[ 0 ]);
        Json.put(result, "paddingTop",    vpad[ 0 ]);
        Json.put(result, "paddingRight",  hpad[ 1 ]);
        Json.put(result, "paddingBottom", vpad[ 1 ]);

        return result.toString();
    }
}
