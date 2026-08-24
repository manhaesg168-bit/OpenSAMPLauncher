package com.umnicode.samp_launcher.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.umnicode.samp_launcher.LauncherApplication
import com.umnicode.samp_launcher.R
import com.umnicode.samp_launcher.UserConfig
import com.umnicode.samp_launcher.core.ServerConfig
import com.umnicode.samp_launcher.core.ServerResolveCallback
import com.umnicode.samp_launcher.core.ServerView
import com.umnicode.samp_launcher.ui.widgets.playbutton.PlayButton
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    private lateinit var rootView: View

    // Servidor fixo do MCRP
    private val MCRP_IP = "181.215.45.38"
    private val MCRP_PORT = "7443"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceStatus: Bundle?
    ): View {
        val sharedPreferences: SharedPreferences? = this.context?.getSharedPreferences("HomeFragment", Context.MODE_PRIVATE);
        val preferencesEditor: SharedPreferences.Editor? = sharedPreferences?.edit();

        this.rootView = inflater.inflate(R.layout.fragment_home, container, false);

        val nicknameText: TextView = this.rootView.findViewById(R.id.nickname)

        val launcherApplication: LauncherApplication = activity?.application as LauncherApplication;
        nicknameText.text = launcherApplication.userConfig.Nickname;

        val portEditText: EditText = this.rootView.findViewById(R.id.port);
        portEditText.filters = Array(1) { PortFilter() };

        val ipEditText: EditText = this.rootView.findViewById(R.id.ip);
        val passwordEditText:EditText = this.rootView.findViewById(R.id.password);

        // Fixa sempre o servidor do MCRP (ignora qualquer valor salvo antes)
        ipEditText.setText(MCRP_IP);
        portEditText.setText(MCRP_PORT);

        if (sharedPreferences != null){
            passwordEditText.setText(sharedPreferences.getString(R.id.password.toString(), ""));
        }

        ipEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateServerConfig();
            }
        });
        portEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateServerConfig();
            }
        });
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                preferencesEditor?.putString(R.id.password.toString(), s.toString());
                preferencesEditor?.apply();
            }
            override fun afterTextChanged(s: Editable?) {
                updateServerConfig();
            }
        });

        val playButton: PlayButton = this.rootView.findViewById(R.id.play_btn) as PlayButton;
        playButton.SetOnSAMPLaunchCallback {
            println("Launch SAMP");
        }

        this.updateServerConfig();
        return this.rootView;
    }

    private fun updateServerConfig(){
        val ipEdit:EditText = this.rootView.findViewById(R.id.ip);
        val portEdit:EditText = this.rootView.findViewById(R.id.port);
        val userConfig:UserConfig = (activity?.application as LauncherApplication).userConfig;

        val IP:String = ipEdit.text.toString();
        val port:Int;

        if (portEdit.text.isNotEmpty()){
            port = portEdit.text.toString().toInt();
        }else{
            port = 0;
        }

        ServerConfig.Resolve(IP, port, userConfig.PingTimeout, this.context, object : ServerResolveCallback {
            override fun OnFinish(OutConfig: ServerConfig?)  {
                val serverView: ServerView = rootView.findViewById(R.id.server_view);
                serverView.SetServer(OutConfig);

                val playButton: PlayButton = rootView.findViewById(R.id.play_btn) as PlayButton;
                playButton.SetServerConfig(OutConfig);
            }

            override fun OnPingFinish(OutConfig: ServerConfig?) {
                val serverView: ServerView = rootView.findViewById(R.id.server_view);
                serverView.SetServer(OutConfig);
            }
        });
    }
}
