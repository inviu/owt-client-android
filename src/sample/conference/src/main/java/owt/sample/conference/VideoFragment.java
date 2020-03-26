/*
 * Copyright (C) 2018 Intel Corporation
 * SPDX-License-Identifier: Apache-2.0
 */
package owt.sample.conference;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.webrtc.RTCStats;
import org.webrtc.RTCStatsReport;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class VideoFragment extends Fragment {
    public static int kMaxStream=8;
    private VideoFragmentListener listener;
    private SurfaceViewRenderer smallRenderer;
    private SurfaceViewRenderer fullRenderer1,fullRenderer2,fullRenderer3,fullRenderer4;
    private TextView[] statsInViews=new TextView[kMaxStream];;
    private TextView statsOutView;
    private float dX, dY;
    private BigInteger lastBytesSent = BigInteger.valueOf(0);
    private BigInteger[] lastBytesReceiveds = new BigInteger[kMaxStream];
    private Long[] lastFrameDecodeds = new Long[kMaxStream];
    private Long lastFrameEncoded = Long.valueOf(0);
//    private View.OnTouchListener touchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            if (v.getId() == R.id.small_renderer) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        dX = v.getX() - event.getRawX();
//                        dY = v.getY() - event.getRawY();
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        v.animate()
//                                .x(event.getRawX() + dX)
//                                .y(event.getRawY() + dY)
//                                .setDuration(0)
//                                .start();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        v.animate()
//                                .x(event.getRawX() + dX >= event.getRawY() + dY ? event.getRawX()
//                                        + dX : 0)
//                                .y(event.getRawX() + dX >= event.getRawY() + dY ? 0
//                                        : event.getRawY() + dY)
//                                .setDuration(10)
//                                .start();
//                        break;
//                }
//            }
//            return true;
//        }
//    };

    public VideoFragment() {
    }

    public void setListener(VideoFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        int resID=kMaxStream==8? R.layout.fragment_video9: R.layout.fragment_video;
        View mView = inflater.inflate(resID, container, false);

//        statsInView = mView.findViewById(R.id.stats_in);
//        statsInView.setVisibility(View.GONE);
        Context ctx=getActivity().getBaseContext();
        for(int i=0;i<kMaxStream;++i){
            int resId = getResources().getIdentifier("stats_in"+(i+1), "id", ctx.getPackageName());
            statsInViews[i] = mView.findViewById(resId);
            statsInViews[i].setVisibility(View.GONE);
        }

        statsOutView = mView.findViewById(R.id.stats_out);
        statsOutView.setVisibility(View.GONE);


//        fullRenderer = mView.findViewById(R.id.full_renderer);
        List<SurfaceViewRenderer> renderers=new ArrayList<>();
        for(int i=0;i<kMaxStream;++i){
            int resId = getResources().getIdentifier("full_renderer"+(i+1), "id", ctx.getPackageName());
            renderers.add(mView.findViewById(resId));
        }

        for (SurfaceViewRenderer render :
                renderers) {
            render.init(((MainActivity) getActivity()).rootEglBase.getEglBaseContext(), null);
            render.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            render.setEnableHardwareScaler(true);
            render.setZOrderMediaOverlay(true);
        }

        //更改预览窗口
        smallRenderer = mView.findViewById(R.id.small_renderer);

        smallRenderer.init(((MainActivity) getActivity()).rootEglBase.getEglBaseContext(), null);
        smallRenderer.setMirror(true);
//        smallRenderer.setOnTouchListener(touchListener);
        smallRenderer.setEnableHardwareScaler(true);
        smallRenderer.setZOrderMediaOverlay(true);

//        fullRenderer.init(((MainActivity) getActivity()).rootEglBase.getEglBaseContext(), null);
//        fullRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
//        fullRenderer.setEnableHardwareScaler(true);
//        fullRenderer.setZOrderMediaOverlay(true);

        listener.onRenderer(smallRenderer, renderers);

        clearStats(true);
        clearStats(false);
        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    void clearStats(boolean outbound) {
//        final TextView statsView = outbound ? statsOutView : statsInView;
        if (outbound) {
            lastBytesSent = BigInteger.valueOf(0);
            lastFrameEncoded = Long.valueOf(0);
        } else {
            for(int i=0;i<kMaxStream;++i){
                lastBytesReceiveds[i] = BigInteger.valueOf(0);
                lastFrameDecodeds[i] = Long.valueOf(0);
            }

        }
        final String statsReport = (outbound ? "\n--- OUTBOUND ---" : "\n--- INBOUND ---")
                + "\nCodec: "
                + "\nResolution: "
                + "\nBitrate: "
                + "\nFrameRate: ";
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(outbound){
                    statsOutView.setVisibility(View.VISIBLE);
                    statsOutView.setText(statsReport);
                }
                else{
                    for(TextView textView:statsInViews){
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(statsReport);
                    }
                }
            }
        });
    }

    void updateStats(RTCStatsReport report, int index) {
        boolean outbound=index<0;
        String codecId = null;
        String codec = "";
        long bytesSR = 0;
        long width = 0, height = 0;
        long frameRate = 0;
        int lost=0;
        for (RTCStats stats : report.getStatsMap().values()) {
            if (stats.getType().equals(outbound ? "outbound-rtp" : "inbound-rtp")) {
                Map<String, Object> members = stats.getMembers();
                if (members.get("mediaType").equals("video")) {
                    codecId = (String) members.get("codecId");
                    if (outbound) {
                        BigInteger bytes = (BigInteger) members.get("bytesSent");
                        bytesSR = bytes.longValue() - lastBytesSent.longValue();
                        lastBytesSent = bytes;
                    } else {
                        BigInteger bytes = (BigInteger) members.get("bytesReceived");
                        bytesSR = bytes.longValue() - lastBytesReceiveds[index].longValue();
                        lastBytesReceiveds[index] = bytes;
                        Integer packetsLost=(Integer) members.get("packetsLost");
                        lost=packetsLost.intValue();
                    }

                    long currentFrame = (long) members.get(outbound ? "framesEncoded" : "framesDecoded");
                    long lastFrame = outbound ? lastFrameEncoded : lastFrameDecodeds[index] ;
                    frameRate = (currentFrame - lastFrame) * 1000
                            / MainActivity.STATS_INTERVAL_MS;
                    if (outbound) {
                        lastFrameEncoded = currentFrame;
                    } else {
                        lastFrameDecodeds[index] = currentFrame;
                    }
                }
            }
            if (stats.getType().equals("track")) {
                Map<String, Object> members = stats.getMembers();
                if (members.get("kind").equals("video")) {
                    width = members.get("frameWidth") == null ? 0 : (long) members.get(
                            "frameWidth");
                    height = members.get("frameHeight") == null ? 0 : (long) members.get(
                            "frameHeight");
                }
            }
        }
        if (codecId != null) {
            codec = (String) report.getStatsMap().get(codecId).getMembers().get("mimeType");
        }

        final String statsReport = (outbound ? "\n--- OUTBOUND ---" : "\n--- INBOUND ---"+ index)
                + "\nCodec: " + codec
                + "\nResolution: " + width + "x" + height
                + "\nBitrate: " + bytesSR * 8 / MainActivity.STATS_INTERVAL_MS + "kbps"
                + "\nFrameRate: " + frameRate
                + (outbound ? "" : "\nLost:"+ lost);
        getActivity().runOnUiThread(() -> {
            if(index>=0){
                statsInViews[index].setVisibility(View.VISIBLE);
                statsInViews[index].setText(statsReport);
            }
            else{
                statsOutView.setVisibility(View.VISIBLE);
                statsOutView.setText(statsReport);
            }
        });
    }

    public interface VideoFragmentListener {
        void onRenderer(SurfaceViewRenderer localRenderer, List<SurfaceViewRenderer> remoteRenderers);
    }
}
