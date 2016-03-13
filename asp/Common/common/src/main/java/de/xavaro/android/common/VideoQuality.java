package de.xavaro.android.common;

public class VideoQuality
{
    public static final int LQ = 0x01;
    public static final int SD = 0x02;
    public static final int HQ = 0x04;
    public static final int HD = 0x08;

    public static int deriveQuality(int scanlines)
    {
        if (scanlines <= 270) return LQ;
        if (scanlines <= 480) return SD;
        if (scanlines <= 576) return HQ;

        return HD;
    }
}
