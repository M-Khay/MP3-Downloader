package com.example.finalmp3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class ViewSongDialog extends SherlockActivity implements OnTouchListener, OnCompletionListener, OnBufferingUpdateListener {
	DownloadsDBAdapter mDbHelper;
	
	public Button  download, share;
	public TextView tv;
	public ImageButton play;
	public SeekBar sb;
	
	
	public Context context=this;
	
	
	
	int ida;
	public String title, url, source;
	
	private MediaPlayer mediaPlayer;
	private int mediaFileLengthInMilliseconds; 

	private Handler handler = new Handler();
	public Runnable notification;
	public ProgressTask task;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);      
        setContentView(R.layout.songview);

	
        
		final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        //Prepare DBHelper
        mDbHelper = new DownloadsDBAdapter(this);
        mDbHelper.open();
        
        //Get Extras
        title = getIntent().getStringExtra("title");
        url = getIntent().getStringExtra("url");
        source = getIntent().getStringExtra("source");
        
        actionBar.setTitle(title);
        
       
    
         play = (ImageButton)this.findViewById(R.id.button_play);
        play.getBackground().setColorFilter(new LightingColorFilter(0xD3D3D3, 0xD3D3D3));
        download = (Button)this.findViewById(R.id.download);
       // download.getBackground().setColorFilter(new LightingColorFilter(0xD3D3D3, 0xD3D3D3));
		sb = (SeekBar)findViewById(R.id.progress_bar);
		
        
		
		
        download.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        			
        		
        		
        		final Dialog dialog=new Dialog(context);
        		dialog.setTitle("Application Alert");
        		
        		
        		
        		dialog.setContentView(R.layout.dialog);
        		Button ok=(Button) dialog.findViewById(R.id.ok);
        		Button cancel=(Button) dialog.findViewById(R.id.cancel);
        		
        		
        		 ok.setOnClickListener(new OnClickListener() {
        	        	@Override
        				public void onClick(View v) {
        	      
        	        		

        	        		download.setEnabled(false);
        	        		        		
        	        	    if(mDbHelper.fetchAllDownloads(url) == 0){
        						mDbHelper.createDownload(title, url, 1, 0);
        						toastmake(getString(R.string.addedqueue));
        	        		}else if(ida != 0){
        						Builder builder = new AlertDialog.Builder(ViewSongDialog.this);
        						builder.setMessage("Would You Like To Re-Download: "+title);
        						builder.setCancelable(false);
        						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        						           @Override
        								public void onClick(DialogInterface dialog, int id) {
        	 					        	   mDbHelper.updateDownloaded(ida, "1");
        										toastmake("Download Readded To The Queue");
        						           }
        						       });
        						builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
        						           @Override
        								public void onClick(DialogInterface dialog, int id) {
        						                dialog.cancel();
        						           }
        						       });
        						AlertDialog alert = builder.create();
        						alert.show();
        	        		}
        	        	    
        	        	    dialog.dismiss();
        	        		
        	        		
        	        	}
        	        	
        		 });
        		
        		

        		 cancel.setOnClickListener(new OnClickListener() {
        	        	@Override
        				public void onClick(View v) {
        	      
        	        		dialog.dismiss();
        	        		
        	        	}
        	        	
        		 });
        		
        		 
        		dialog.show();
        		
        		
        		
        	}
        });
		play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(task == null){
					play.setClickable(false);
					toastmake("Song Loading");
					task=new ProgressTask();
					task.execute(); 
				}else{
					if(!mediaPlayer.isPlaying()){
						mediaPlayer.start();
						handler.removeCallbacks(notification);
						primarySeekBarProgressUpdater();
						play.setImageResource(R.drawable.ic_media_pause);
					}else {
						mediaPlayer.pause();
						play.setImageResource(R.drawable.ic_media_play);
					}
				}
        }});
        
        sb.setMax(99);
        sb.setOnTouchListener(this);

		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnBufferingUpdateListener(this);
		mediaPlayer.setOnCompletionListener(this);

		FindLyrics fl = new FindLyrics();
		fl.execute();
		
        //Ads Test
		DisplayMetrics dm = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(dm); 
	}
	
	class FindLyrics extends AsyncTask<Void, Void, Void>{
		String lyrics; 
		int fail = 0;
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try{
				String searchQuery = title.replaceAll("[^ \\w]", "");
				searchQuery = searchQuery.replace(" ", "+");
				String searchResult = httpRun("http://search.azlyrics.com/search.php?q="+searchQuery).toString();
				System.out.println("http://search.azlyrics.com/search.php?q="+searchQuery);
				if(!searchResult.contains("Sorry, your search returned") && !searchResult.equals("")){
					String link = searchResult.split("<div class=\"sen\">")[1];
					link = link.substring(link.indexOf("<a href=\""), link.indexOf("\">"));
					link = link.replace("<a href=\"", "");
					
					String lR = httpRun(link).toString();
					if(lR != "" && lR.contains("<!-- start of lyrics -->")){
						lR = lR.substring(lR.indexOf("<!-- start of lyrics -->"), lR.indexOf("<!-- end of lyrics -->"));
						lR = lR.replace("<!-- start of lyrics -->", "");
						lyrics = lR;
					}else{
						fail = 1;
						lyrics = "Couldn't find lyrics";
					}
				}else{
					fail = 1;
					lyrics = "Couldn't find lyrics";
				}
			}catch(Exception e){
				fail = 1;
		    	e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
//			TextView tv = (TextView)findViewById(R.id.lyrics_text);
//			if(lyrics != null && lyrics != "" && fail != 1){
//				tv.setText(Html.fromHtml(lyrics));
//			}else{
//				tv.setText(Html.fromHtml("FAILED LOADING LYRICS"));
//			
//			}
		}
		
	}
	
	public ByteArrayOutputStream httpRun(String url){
		ByteArrayOutputStream output = null;
		try{
            DefaultHttpClient httpClient = new DefaultHttpClient();
            BasicHttpContext httpContext = new BasicHttpContext();
            HttpGet httpPost = new HttpGet(url);
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36");
            HttpResponse response = httpClient.execute(httpPost, httpContext);
    	    InputStream result = null;
    	    
            result = response.getEntity().getContent();
            
    		byte[] buffer = new byte[ 1024 ];
    		int size = 0;
    		output = new ByteArrayOutputStream();
    			
    		while( (size = result.read( buffer ) ) != -1 ) {
    			output.write( buffer, 0, size );
    		}

	    } catch (ClientProtocolException e) {
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } catch (java.lang.IllegalStateException e){
	    	e.printStackTrace();
	    }
		return output;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				handler.removeCallbacks(notification);
				if(mediaPlayer.isPlaying()){
					mediaPlayer.stop();
				}
				mediaPlayer.reset();
				
				mediaPlayer.release();
				
				mDbHelper.close();
				
		    	Intent i = new Intent(getBaseContext(), MainActivity.class);
		    	i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); 
		    	ViewSongDialog.this.startActivity(i);
		    	finish();
				return true;
		}
		return false;
	}
	
	public void toastmake(String title){
		Toast.makeText(this, title, Toast.LENGTH_LONG).show();
	}
	
	class ProgressTask extends AsyncTask<Integer, Integer, Void>{
		  @Override
		  protected Void doInBackground(Integer... params) {
				try {
					String location;
					if(source.contains("hulkshare") || source.contains("zing")){
						HttpURLConnection con = (HttpURLConnection)(new URL( url ).openConnection());
						con.setInstanceFollowRedirects( false );
						con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");
						con.connect();
						location = con.getHeaderField( "Location" );
					}else{
						location = url;
					}
					mediaPlayer.setDataSource(location); 
					mediaPlayer.prepare(); 
					publishProgress(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
		  }
		  
		  protected void onProgressUpdate(Integer integers) {
			  super.onProgressUpdate();
			  if(integers == 1) {
			    Toast.makeText(ViewSongDialog.this, getString(R.string.buffering), Toast.LENGTH_SHORT).show(); 
			  }
		  }		
		  
		  @Override
		  protected void onPostExecute(Void result) {
			  mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the song length in milliseconds from URL
			
			  try{
				  if(!mediaPlayer.isPlaying()){
					  mediaPlayer.start();
					  play.setClickable(true);
					  play.setImageResource(R.drawable.ic_media_pause);
				  }else {
					  toastmake(getString(R.string.failedloading));
					  mediaPlayer.pause();
					  play.setClickable(false);
					  play.setImageResource(R.drawable.ic_media_play);
				  }
			  }catch (IllegalStateException e){
				  toastmake(getString(R.string.failedloading));
				  play.setClickable(false);
				  play.setImageResource(R.drawable.ic_media_play);
			  }
			  handler.removeCallbacks(notification);
			  primarySeekBarProgressUpdater();
				
		  }
	}
    
	@Override
	public void onBackPressed() {
		handler.removeCallbacks(notification);
		if(mediaPlayer.isPlaying()){
			mediaPlayer.stop();
		}
		mediaPlayer.reset();
		
		mediaPlayer.release();
		
		mDbHelper.close();
		
    	Intent i = new Intent(getBaseContext(), MainActivity.class);
    	i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); 
    	ViewSongDialog.this.startActivity(i);
    	finish();
	    return;
	}

    private void primarySeekBarProgressUpdater() {
    	sb.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaFileLengthInMilliseconds)*100)); // This math construction give a percentage of "was playing"/"song length"
		if (mediaPlayer.isPlaying()) {
			notification = new Runnable() {
		        @Override
				public void run() {
		        	primarySeekBarProgressUpdater();
				}
		    };
		    handler.postDelayed(notification, 500);
    	}
		
    }
    
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(v.getId() == R.id.progress_bar){
			if(mediaPlayer.isPlaying()){
		    	SeekBar sb = (SeekBar)v;
				int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
				mediaPlayer.seekTo(playPositionInMillisecconds);
			}
		}
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		play.setImageResource(R.drawable.ic_media_play);
		
		mediaPlayer.stop();
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		sb.setSecondaryProgress(percent);
	}
}