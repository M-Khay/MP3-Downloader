 package com.example.finalmp3;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.krmkc.nqcwv93000.AdListener;
import com.krmkc.nqcwv93000.AdView;

public class MainActivity extends SherlockFragmentActivity implements ActionBar.TabListener {
	
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;	
	
	Fragment search = new SearchFragment();
	Fragment downloads = new DownloadFragment();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
		
		RelativeLayout relativeLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
				RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
				RelativeLayout.TRUE);

		AdView adView = new AdView(MainActivity.this,AdView.BANNER_TYPE_IMAGE, AdView.PLACEMENT_TYPE_INTERSTITIAL,true, false, AdView.ANIMATION_TYPE_LEFT_TO_RIGHT);
		adView.setAdListener(adlistener);
		relativeLayout.addView(adView, layoutParams);

		MainActivity.this.addContentView(relativeLayout, layoutParams1);
		
		
		
		startService(new Intent(this, DownloadService.class));
		
		
		
		
		
		
		
		//Prepare Pre-Requisites
		String path=Environment.getExternalStorageDirectory()+"/music/"+getString(R.string.app_name);  
		boolean exists = (new File(path)).exists();
		if (!exists){
			new File(path).mkdirs();
		}
		
		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
	}
	
	

	AdListener.MraidAdListener adlistener = new AdListener.MraidAdListener() {

		@Override
		public void onAdClickListener() {
			// This will get called when ad is clicked.
		}

		@Override
		public void onAdLoadedListener() {
			// This will get called when an ad has loaded.
		}

		@Override
		public void onAdLoadingListener() {
			// This will get called when a rich media ad is loading.
		}

		@Override
		public void onAdExpandedListner() {
			// This will get called when an ad is showing on a user's screen.
			// This may cover the whole UI.
		}

		@Override
		public void onCloseListener() {
			// This will get called when an ad is closing/resizing from an
			// expanded state.
		}

		@Override
		public void onErrorListener(String message) {
			// This will get called when any error has occurred. This will also
			// get called if the SDK notices any integration mistakes.
		}

		@Override
		public void noAdAvailableListener() {
			// this will get called when ad is not available

		}
	};

	
	@Override
	public void onTabSelected(Tab tab, android.support.v4.app.FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, android.support.v4.app.FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, android.support.v4.app.FragmentTransaction ft) {
	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position){
				case 0:
					fragment = search;
					break;
				case 1:
					fragment = downloads;
					break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return getString(R.string.fragment_one).toUpperCase();
				case 1:
					return getString(R.string.fragment_two).toUpperCase();
			}
			return null;
		}
	}
	
	
}

