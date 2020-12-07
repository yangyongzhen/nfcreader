package com.ksxy.nfc.demo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.taiyun.mobilereader.NFCReaderHelper;
import cn.com.taiyun.mobilereader.UserInfo;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class MainActivity extends Activity {
	private static ImageView iv_zhaopian;
	static Handler uiHandler = null;

	//TODO： 试用appkey和appsecret
	private static String appKey = "950c89197f66";
	private static String appSecret = "2848501bc3bd";

	NfcAdapter mNfcAdapter;
	TextView uuIdText;
	public NFCReaderHelper mNFCReaderHelper;
	PendingIntent mNfcPendingIntent;
	TextView tvname;
	TextView tvsex;
	TextView tvnation;
	TextView tvbirthday;
	TextView tvcode;
	TextView tvaddress;
	TextView tvdate;
	TextView tvdepar;
	TextView readerstatText;
	TextView tvshijiancontent;
	Button buttonset;
	private Context context = null;

	/**
	 * 是否使用本地so解析头像
	 */
	private Boolean isShowImage = true;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		uuIdText = ((TextView) findViewById(R.id.tvuuid));
		iv_zhaopian = (ImageView) findViewById(R.id.ivHead);
		tvname = (TextView) findViewById(R.id.tvname2);
		tvsex = (TextView) findViewById(R.id.tvsex2);
		tvnation = (TextView) findViewById(R.id.tvnation2);
		tvbirthday = (TextView) findViewById(R.id.tvbirthday2);
		tvcode = (TextView) findViewById(R.id.tvcode2);
		tvaddress = (TextView) findViewById(R.id.tvaddress2);
		tvdate = (TextView) findViewById(R.id.tvdate2);
		tvdepar = (TextView) findViewById(R.id.tvdepart2);
		readerstatText = (TextView) findViewById(R.id.readerstatText);
		tvshijiancontent = (TextView) findViewById(R.id.tvshijiancontent);
		buttonset = (Button) findViewById(R.id.buttonset);
		buttonset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, ConfigActivity.class);
				startActivity(intent);
			}
		});

		context = this;
		uiHandler = new MyHandler(this);

		// 设备注册
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		// 判断设备是否可用
		if (mNfcAdapter == null) {
			toast("该设备不支持nfc!");
			return;
		}

		if ((null != mNfcAdapter) && !mNfcAdapter.isEnabled()) {
			Toast.makeText(this, "请在系统设置中先启用NFC功能", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
			finish();

			return;
		}

		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		mNFCReaderHelper = new NFCReaderHelper(this, uiHandler, appKey,
				appSecret);
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			if (null != mNfcPendingIntent) {
			    IntentFilter[] arrayOfIntentFilter = new IntentFilter[0];

				mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
						arrayOfIntentFilter, null);
				resolvIntent(getIntent());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			// 0-否，1-是
			isShowImage = "1".equals(SpUtil.getString(context,
					"touxiang_set", "1"));

		} catch (Exception ex) {
		}
	
	}

	@Override
	protected void onPause() {
		super.onPause();
		//TODO: A8POS 需要注释掉	
		try {
			if (mNfcAdapter != null) {
				mNfcAdapter.disableForegroundDispatch(this);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		try {
			setIntent(intent);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	synchronized void resolvIntent(Intent intent) {
		try {
			String action = intent.getAction();

			if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
					|| NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
				new NFCReadTask(intent, context).execute();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	private class NFCReadTask extends AsyncTask<Void, Void, String> {
		private Intent mIntent = null;
		private long beginTime;

		public NFCReadTask(Intent i, Context contextTemp) {
			mIntent = i;
			context = contextTemp;
		}

		@Override
		protected String doInBackground(Void... params) {

			beginTime = System.currentTimeMillis();
			String strCardInfo = mNFCReaderHelper.readCardWithIntent(mIntent);
			// 获取uuid
			String uuid = mNFCReaderHelper.readCardUUId();
			return uuid + "," + strCardInfo;
		}
		
		@Override
		protected void onPostExecute(String strCardInfo) {
			super.onPostExecute(strCardInfo);

			String uuid = "";
			try {
				uuid = strCardInfo.split(",")[0];
				strCardInfo = strCardInfo.split(",")[1];
			} catch (Exception ex) {

			}
			uuIdText.setText(uuid );
			tvshijiancontent.setText((System.currentTimeMillis() - beginTime)
					+ "毫秒");
			
			if ((null != strCardInfo) && (strCardInfo.length() > 1500)) {
				UserInfo userInfo = mNFCReaderHelper
						.parsePersonInfoNew(strCardInfo);
				if(null != userInfo) {
					tvname.setText(userInfo.name);
					tvsex.setText(userInfo.sex);
					tvnation.setText(userInfo.nation);
					tvbirthday.setText(userInfo.brithday);
					tvcode.setText(userInfo.id);
					tvaddress.setText(userInfo.address);
					tvdate.setText(userInfo.exper + "-" + userInfo.exper2);
					tvdepar.setText(userInfo.issue);				
					//是否显示照片
					if (isShowImage) {
						// 说明：mNFCReaderHelper.decodeImageByte(strCardInfo) 返回的是照片信息
						byte[] bmpBuff = new byte[38862];
						int nRet = mNFCReaderHelper.GetPicByBuff(mNFCReaderHelper
								.decodeImageByte(strCardInfo),bmpBuff);
						if(nRet == 1){
							Bitmap bm = BitmapFactory.decodeByteArray(
									bmpBuff, 0, 38862);
							iv_zhaopian.setImageBitmap(bm);	
						} else {
							toast("照片解析失败 ");
						}
					} 
				}
			}
		}
	}

	

	class MyHandler extends Handler {
		private MainActivity activity;

		MyHandler(MainActivity activity) {
			this.activity = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			//授权结果
			case 999:
				String msgTemp = (String) msg.obj;
				readerstatText.setText(msgTemp);
				break;
			//身份证读取中
			case 1000:
				 msgTemp = (String) msg.obj;
					readerstatText.setText(msgTemp);
				break;
			}
		}
	}
	
	
	
	

}
