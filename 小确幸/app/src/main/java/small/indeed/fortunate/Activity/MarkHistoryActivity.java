package small.indeed.fortunate.Activity;

import android.os.Bundle;  
import android.support.design.widget.TabLayout;  
import android.support.v4.app.Fragment;  
import android.support.v4.view.ViewPager;  
import android.support.v7.app.AppCompatActivity;  

import java.util.ArrayList;  
import java.util.List;
import small.indeed.fortunate.*;
import small.indeed.fortunate.Fragment.*;
import android.support.v4.app.*;  

public class MarkHistoryActivity extends AppCompatActivity {
	private TabLayout tabLayout;  
	private ViewPager viewPager;  
	private List<String> titles = new ArrayList<String>();  
	private List<Fragment> fragments = new ArrayList<Fragment>();
	
	@Override  
	protected void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.fragment_mark_history);  
		initView();
	}  
	
	private void initView() {  
		tabLayout = (TabLayout) findViewById(R.id.tabLayout_mark_history);  
		viewPager = (ViewPager) findViewById(R.id.viewPager_mark_history);  
		titles.add("书签列表");  
		titles.add("历史记录");  
		
		fragments.add(new MarkHistoryFragment());  
		fragments.add(new MarkHistoryFragment());  
		
		MyFragmentAdapter adapter = new MyFragmentAdapter(getSupportFragmentManager(), titles, fragments);  
		viewPager.setAdapter(adapter);  

		tabLayout.setupWithViewPager(viewPager);  
		tabLayout.setTabsFromPagerAdapter(adapter);
	}
	
	private class MyFragmentAdapter extends FragmentPagerAdapter {
		
		private List<String> title;  
		private List<Fragment> views;  

		public MyFragmentAdapter(FragmentManager fm, List<String> title, List<Fragment> views) {  
			super(fm);  
			this.title = title;  
			this.views = views;  
		}  
		@Override  
		public Fragment getItem(int position) {  
			return views.get(position);  
		}  
		@Override  
		public int getCount() {  
			return views.size();  
		}  
		//配置标题的方法  
		@Override  
		public CharSequence getPageTitle(int position) {  
			return title.get(position);  
		}
	}
}
