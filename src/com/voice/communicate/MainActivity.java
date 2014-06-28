package com.voice.communicate;

import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView labelMessage;
	EditText inputMessageEditText;
	Button sendButton;
	VoiceReceiver receiver;
	VoiceSender sender;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		labelMessage = (TextView) findViewById(R.id.lableMessage);
		inputMessageEditText = (EditText) findViewById(R.id.inputMessage);
		sendButton = (Button) findViewById(R.id.button1);
		receiver = new VoiceReceiver();
		receiver.setOnReceiveMessage(new IOnReceiveMessage() {

			@Override
			public void onReceive(byte[] message) {
				labelMessage.setText(new String(message));
			}
		});
		receiver.start();
		sender = new VoiceSender();
		sender.start();
		sendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (inputMessageEditText.getText().length() > 0) {
					setTitle(sender.send(inputMessageEditText.getText()
							.toString()));
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.KEYCODE_BACK == event.getKeyCode()) {
			System.exit(0);
		}
		return false;
	}
}
