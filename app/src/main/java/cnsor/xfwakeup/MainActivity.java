package cnsor.xfwakeup;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;

import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    private String TAG = "ivw";
    private Toast mToast;
    private TextView wakeupResult;
    private VoiceWakeuper mIvw;
    // 唤醒结果内容
    private String resultString;
    //门限值 越小就越容易被唤醒 不懂啊
    private int curThresh = 0;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID + "=56516737");
        //初始化UI资源
        initResource();
        //初始化唤醒引擎
        initWakeupEngine();
        //程序运行后自动开启监听唤醒模式
        startWakeupListen();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initResource() {
        wakeupResult = (TextView) findViewById(R.id.textView);
        wakeupResult.setText("等待唤醒中....");
    }

    /*初始化监听类*/
    private void initWakeupEngine() {
        // 加载识唤醒地资源，resPath为本地识别资源路径
        StringBuffer param = new StringBuffer();
        String resPath = ResourceUtil.generateResourcePath(MainActivity.this,
                RESOURCE_TYPE.assets, "ivw/56516737.jet");

        param.append(SpeechConstant.IVW_RES_PATH + "=" + resPath);
        param.append("," + ResourceUtil.ENGINE_START + "=" + SpeechConstant.ENG_IVW);
        boolean ret = SpeechUtility.getUtility().setParameter(
                ResourceUtil.ENGINE_START, param.toString());
        if (!ret) {
            Log.d(TAG, "启动本地引擎失败！");
        }
        // 初始化唤醒对象
        mIvw = VoiceWakeuper.createWakeuper(MainActivity.this, null);
    }

    /*开始监听唤醒*/
    private void startWakeupListen() {
//非空判断，防止因空指针使程序崩溃
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            resultString = "";
            wakeupResult.setText(resultString);
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            /**
             * 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
             * 示例demo默认设置第一个唤醒词，建议开发者根据定制资源中唤醒词个数进行设置
             */
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:"
                    + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
            mIvw.startListening(mWakeuperListener);
        } else {
            showTip("唤醒未初始化");
        }
    }

    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 " + text);
                buffer.append("\n");
                buffer.append("【操作类型】" + object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】" + object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
                resultString = buffer.toString();
            } catch (JSONException e) {
                resultString = "结果解析出错";
                e.printStackTrace();
            }
            wakeupResult.setText(resultString);
        }

        @Override
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onBeginOfSpeech() {
            showTip("开始说话");
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {

        }

        @Override
        public void onVolumeChanged(int volume) {
            // TODO Auto-generated method stub

        }
    };

    private void showTip(final String str) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //mToast.setText(str);

                //mToast.show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://cnsor.xfwakeup/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://cnsor.xfwakeup/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy WakeDemo");
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.destroy();
        } else {
            showTip("唤醒未初始化");
        }
    }
}
