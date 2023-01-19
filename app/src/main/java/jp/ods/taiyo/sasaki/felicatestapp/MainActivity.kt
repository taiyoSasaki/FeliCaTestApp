package jp.ods.taiyo.sasaki.felicatestapp

import android.app.assist.AssistContent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mHandler = Handler()
    private var mTimer: Timer? = null

    lateinit var nfcAdapter: NfcAdapter
    private val TIME_OUT = 200 //カードとの通信タイムアウト200ms

    private val filename = "nfc.txt"

    private var LoopFlag = false
    private var LoopNum = 100 //100回polling

    private var mResult = ""
    private var mTotal = 0
    private var mSuccess = 0
    private var mFail = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //トグルの初期設定
        start_button.isEnabled = true
        Loop_button.isEnabled = true
        stop_button.isEnabled = false

        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        //onClickイベント
        start_button.setOnClickListener {
            LoopFlag = false
            startReaderMode()
        }

        Loop_button.setOnClickListener {
            LoopFlag = true
            startReaderMode()
        }

        stop_button.setOnClickListener {
            stopReaderMode()
        }
    }

    private fun startReaderMode() {
        //トグル設定
        start_button.isEnabled = false
        Loop_button.isEnabled = false
        stop_button.isEnabled = true

        //TextViewの初期化
        IdmText.text = ""
        PmmText.text = ""
        ResultText.text = ""
        DiscriminantText.text = ""


        nfcAdapter.enableReaderMode(this, MyReaderCallback(), NfcAdapter.FLAG_READER_NFC_F, null);
        //タイムアウト処理の実行
        mTimer = Timer()
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                Log.d("timer", "カードが見つかりません")
                discriminant(false)
            }
        }, TIME_OUT, TIME_OUT)

    }

    private fun stopReaderMode() {
        //トグル設定
        start_button.isEnabled = true
        Loop_button.isEnabled = true
        stop_button.isEnabled = false

        //ファイルを保存するディレクトリを指定
        val context = applicationContext
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),filename)

        nfcAdapter.disableReaderMode(this)

        saveFile(file, mResult)

    }

    private class MyReaderCallback :NfcAdapter.ReaderCallback {
        override fun onTagDiscovered(tag: Tag?) {
            //タグが見つかった時に処理
            Log.d("FeliCa", "onTagDiscover")

        }

    }

    private fun startPolling() {

    }

    private fun discriminant(Flag : Boolean) :String{
        mTotal++
        if (Flag){
            mSuccess++
            return "o"
        } else{
            mFail++
            return "."
        }
    }


    //bytes型を16進数型文字列へ変換
    private fun byteToHex(b : ByteArray) : String{
        var s : String = ""
        for (i in 0..b.size-1){
            s += "[%02X]".format(b[i])
        }
        return s
    }

    // ファイルを保存
    private fun saveFile(file: File?, str: String) {
        // try-with-resources
        try {
            FileOutputStream(file).use { output ->
                output.write(str.toByteArray())
                Log.d("txtFile", "save file")
                Toast.makeText(this, "nfc.txtを保存しました", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Log.e("txtFile", "cant save file")
        }
    }

}