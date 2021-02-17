package io.github.pranavgade20.pbridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrackpadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrackpadFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PORT = "port";
    private static final String ARG_IP = "ip";

    private static int port;
    private static String ip;

    private static Thread thread;
    private static Socket socket;
    private static TextWatcher watcher;
    private static ImageView trackpad;
    private static Context context;

    public TrackpadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param ip ip address to connect to
     * @param port port where to start.
     * @return A new instance of fragment TrackpadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrackpadFragment newInstance(String ip, int port) {
        TrackpadFragment fragment = new TrackpadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IP, ip);
        args.putInt(ARG_PORT, port);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ip = getArguments().getString(ARG_IP);
            port = getArguments().getInt(ARG_PORT);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View ret = inflater.inflate(R.layout.fragment_trackpad, container, false);

        ret.setOnTouchListener((v, event) -> true);
        context = getContext();
        trackpad = ret.findViewById(R.id.trackpad);

        connect();

        return ret;
    }


    @SuppressLint("ClickableViewAccessibility")
    public static void connect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (thread != null) {
            thread = null; // todo make sure this is cleaned up nicely
        }
        try {
            trackpad.setOnTouchListener(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        (thread = new Thread(() -> {
            try {
                Looper.prepare(); // required for toast
                socket = new Socket(ip, port);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());

                writer.write("hi\n");
                writer.flush();

                boolean[] isTap = {false};
                double[] coords = {0, 0};
                trackpad.setOnTouchListener((v, event) -> {
                    try {
                        double x = event.getX()/v.getWidth();
                        double y = event.getY()/v.getHeight();

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                isTap[0] = true;
                                coords[0] = x;
                                coords[1] = y;
                                break;
                            case MotionEvent.ACTION_MOVE:
                                new Thread(() -> {
                                    try {
                                        writer.write("m " + (x-coords[0]) + " " + (y-coords[1]) + "\n");
                                        coords[0] = x;
                                        coords[1] = y;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                                isTap[0] = false;
                            case MotionEvent.ACTION_UP:
                                new Thread(() -> {
                                    try {
                                        if (isTap[0]) writer.write("t 1\n");
                                        writer.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return true;
                });

                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        if (line.isEmpty()) continue;
                        if (line.equals("hi")) {
                            Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {}
                reader.close();
                writer.close();
                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }
}
