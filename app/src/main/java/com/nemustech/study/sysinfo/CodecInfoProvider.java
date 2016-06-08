package com.nemustech.study.sysinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.util.Log;
import android.util.Range;

import java.util.ArrayList;

/**
 * Created by cheolgyoon on 2016. 6. 7..
 *
 */
public class CodecInfoProvider extends InfoProvider {
    @SuppressWarnings("unused")
    private static final String TAG = CodecInfoProvider.class.getSimpleName();

    private static ArrayList<InfoItem> sCodecItems;

    CodecInfoProvider(Context context) {
        super(context);
    }

    @SuppressLint("NewApi")
    private void addCapability(StringBuilder sb, MediaCodecInfo.CodecCapabilities mccap) {
        MediaCodecInfo.AudioCapabilities ap = mccap.getAudioCapabilities();
        if (null == ap) {
            sb.append("\nNo audio capability.");
        } else {
            sb.append("\nAudio")
                    .append("\n\tBitrate range: " + ap.getBitrateRange())
                    .append("\n\tMaximum input channel: " + ap.getMaxInputChannelCount());
            try {
                Range<Integer>[] srrs = ap.getSupportedSampleRateRanges();
                if (null != srrs && 0 < srrs.length) {
                    sb.append("\n\tSupported sample rate ranges");
                    for (Range<Integer> srr : srrs) {
                        sb.append("\n\t\t" + srr);
                    }
                }
                int[] srr = ap.getSupportedSampleRates();
                if (null != srr && 0 < srr.length) {
                    sb.append("\n\tSupported sample rates");
                    for (int rate : srr) {
                        sb.append("\n\t\t" + rate);
                    }
                }
            } catch (NullPointerException npe) {
                Log.e(TAG, npe.toString());
                sb.append("\n\tInternal error retrieving supported sample rates");
            }
        }

        MediaCodecInfo.VideoCapabilities vp = mccap.getVideoCapabilities();
        if (null == vp) {
            sb.append("\nNo video capability.");
        } else {
            sb.append("\nVideo")
                    .append("\n\tBitrate range: " + vp.getBitrateRange())
                    .append("\n\tFrame rate range: " + vp.getSupportedFrameRates())
                    .append("\n\tSupported widths: " + vp.getSupportedWidths())
                    .append("\n\tSupported heights: " + vp.getSupportedHeights())
                    .append("\n\tWidth alignment: " + vp.getWidthAlignment())
                    .append("\n\tHeight alignment: " + vp.getHeightAlignment());
        }

        MediaCodecInfo.EncoderCapabilities ep = mccap.getEncoderCapabilities();
        if (null == ep) {
            //  Nothing to do here
        } else {
            sb.append("\nEncoding").append("\n\tComplexity range: " + ep.getComplexityRange());
            boolean cbr = ep.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
            boolean cq = ep.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ);
            boolean vbr = ep.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
            if (cbr || cq || vbr) {
                sb.append("\n\tSupported Bitrate mode");
                if (cbr) {
                    sb.append("\n\t\tConstant bitrate");
                }
                if (cq) {
                    sb.append("\n\t\tConstant quality");
                }
                if (vbr) {
                    sb.append("\n\t\tVariable bitrate");
                }
            }
        }
        sb.append('\n');
    }

    @SuppressLint("NewApi")
    private void addTypeInfo(StringBuilder sb, MediaCodecInfo info, String type) {
        sb.append(type).append(info.isEncoder()? " encoder": "");
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            MediaCodecInfo.CodecCapabilities mccap = info.getCapabilitiesForType(type);
            addCapability(sb, mccap);
        }
    }

    @SuppressLint("NewApi")
    private InfoItem getCodecInfoItem(MediaCodecInfo info) {
        String[] types = info.getSupportedTypes();
        StringBuilder sb = new StringBuilder();
        if (null == types || 0 == types.length) {
            sb.append(getString(R.string.unsupported));
        } else {
            addTypeInfo(sb, info, types[0]);
            for (int idx = 1; idx < types.length; ++idx) {
                addTypeInfo(sb, info, types[idx]);
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return new InfoItem(info.getName(), sb.toString());
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    ArrayList<InfoItem> getItems() {
        if (null == sCodecItems) {
            sCodecItems = new ArrayList<>();
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                sCodecItems.add(new InfoItem(getString(R.string.codec_info), getString(R.string.sdk_version_required, Build.VERSION_CODES.JELLY_BEAN)));
            } else if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                int count = MediaCodecList.getCodecCount();
                for (int idx = 0; idx < count; ++idx) {
                    MediaCodecInfo info = MediaCodecList.getCodecInfoAt(idx);
                    sCodecItems.add(getCodecInfoItem(info));
                }
            } else {
                MediaCodecList mcl = new MediaCodecList(MediaCodecList.ALL_CODECS);
                MediaCodecInfo[] infos = mcl.getCodecInfos();
                if (null == infos || 0 == infos.length) {
                    sCodecItems.add(new InfoItem(getString(R.string.codec_info), getString(R.string.codec_none)));
                } else {
                    for (MediaCodecInfo info: infos) {
                        sCodecItems.add(getCodecInfoItem(info));
                    }
                }
            }
        }
        return sCodecItems;
    }

    @Override
    protected Object[] getInfoParams() {
        return new Object[0];
    }

    @Override
    protected InfoItem getItem(int infoId, Object... params) {
        return null;
    }
}
