package com.example.labworksconsumeapi

import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.UserManager
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import vendor.labworks.serialportmanager.SerialPortManager
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity() {

    private val job = Job()
    private val coroutineContext : CoroutineContext get() = job + Dispatchers.IO
    private val scope = CoroutineScope(coroutineContext)

    override fun onResume() {
        super.onResume()
        // full screen mode
        // full screen mode
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

            serialPortRx()


        button.setOnClickListener {
            serialPortTx()
        }

        val packages = arrayOf(this.packageName)
        val dpm: DevicePolicyManager
        val cn: ComponentName
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        cn = ComponentName(this, DevAdmin::class.java)
        dpm.addUserRestriction(cn, UserManager.DISALLOW_ADJUST_VOLUME)
        dpm.setLockTaskPackages(cn, packages)
        startLockTask()


    }


    private fun serialPortRx(){
        scope.launch() {
            try{
                val sp = SerialPortManager.getInstance()
                runBlocking {
                    val ch = sp.rx().toChar()

                    launch(Dispatchers.Main) {
                        tvRx.text = "${tvRx.text} $ch"
                    }
                    serialPortRx()
                }
            }catch (e : Exception){
                showErrorMessage("Error in Rx", e.message ?: "error")
            }
        }
    }

    fun serialPortTx(){
        try{
            val sp = SerialPortManager.getInstance()
            val txt = editText.text
            for(index in txt.indices){
                val b = txt[index]
                sp.tx(b.toByte())
            }
            editText.text.clear()
        }catch (e : Exception){
            showErrorMessage("Error on TX!", e.message ?: "erro")
        }
    }


    fun showErrorMessage(title : String, msg : String){
        val alertDialog: AlertDialog = android.app.AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(msg)
        alertDialog.show()
    }
}
