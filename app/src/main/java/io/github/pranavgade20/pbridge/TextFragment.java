package io.github.pranavgade20.pbridge;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TextFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TextFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PORT = "port";
    private static final String ARG_IP = "ip";

    private static int port;
    private static String ip;

    private static Thread thread;
    private static Socket socket;
    private static TextWatcher watcher;
    private static EditText editText;
    private static Context context;

    public TextFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param ip ip address to connect to
     * @param port port where to start.
     * @return A new instance of fragment TextFragment.
     */
    public static TextFragment newInstance(String ip, int port) {
        TextFragment fragment = new TextFragment();
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
            port = getArguments().getInt(ARG_PORT);
            ip = getArguments().getString(ARG_IP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View ret = inflater.inflate(R.layout.fragment_text, container, false);

        context = getContext();
        editText = ret.findViewById(R.id.editText);

        connect();

        return ret;
    }

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
            editText.removeTextChangedListener(watcher);
        } catch (Exception e) {
            e.printStackTrace();
        }

        (thread = new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());

                writer.write("hi\n");
                writer.flush();
                final int[] prev = {0};
                editText.addTextChangedListener(watcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        (new Thread(() -> {
                            try {
                                if (prev[0] != start) {
                                    prev[0] = start;
                                    writer.write("cls\n");
                                }
                                writer.write("s ");
                                writer.write(s.toString().substring(start, start+count).replaceAll("\\n", "\\\\n"));
                                writer.write("\n");
                                writer.flush();
                            } catch (IOException e) { }
                        })).start();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        if (line.isEmpty()) continue;
                        if (line.equals("hi")) {
                            Looper.prepare();
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
