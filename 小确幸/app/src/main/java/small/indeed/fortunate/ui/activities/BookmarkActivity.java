package small.indeed.fortunate.ui.activities;

import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.net.*;
import android.os.*;
import android.support.design.*;
import android.util.*;
import android.view.*;
import android.view.ContextMenu.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import java.util.*;
import small.indeed.fortunate.db.*;

import android.view.View.OnCreateContextMenuListener;

public class BookmarkActivity extends Activity {

		ArrayList<HashMap<String, Object>> history_data_list = new ArrayList<HashMap<String, Object>>();// 用来显示历史的list
		private SQLiteHelper sqliteHelper;
		private Cursor myCursor;
		private ListView history_listview;
		public String operaString = null;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_bookmark); // 加载listview布局
			init(); // 绘制listview
		}

		/* 绘制listview */
		public void init() {
			sqliteHelper = new SQLiteHelper(getApplicationContext()); // 实例sql
			history_listview = (ListView) findViewById(R.id.history_list); // 绑定listview

			SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), // 设置绘图listview
													  get_History(), R.layout.item_list_bookmark, new String[] { "网页", "网址" },
													  new int[] { R.id.website_name, R.id.website_url });
			history_listview.setAdapter(adapter); // 更新绘制

			// 设置ListView的项目按下事件监听
			history_listview.setOnItemClickListener(new OnItemClickListener() {

					public void onItemClick(AdapterView<?> parent, View v, int position, long id) { // 按下
						String url = history_data_list.get(position).get("网址") // 获取网址传入
							.toString();
						Intent intent = new Intent(getApplicationContext(), BrowserActivity.class);
						intent.setData(Uri.parse(url));
						startActivity(intent);
						finish();  //关闭书签
					}
				});

			// 设置ListView的项目长按下事件监听
			history_listview.setOnItemLongClickListener(new ListItemLongClick());
			history_listview.setOnCreateContextMenuListener(new ListonCreate());

		}

		// 长按弹出操作事件类
		private class ListonCreate implements OnCreateContextMenuListener {

			public void onCreateContextMenu(ContextMenu menu, View arg1, ContextMenuInfo arg2) {
				menu.add(0, 0, 0, "删除");
			}

		}

		// 长按事件类
		private class ListItemLongClick implements OnItemLongClickListener {

			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long arg3) {
				Log.e("tag", history_data_list.get(position).get("网页").toString());
				operaString = history_data_list.get(position).get("网页").toString();
				return false;
			}

		}

		/* 删除操作 */
		public boolean onContextItemSelected(MenuItem item) {
			switch (item.getItemId()) {
				case 0:
					// 删除操作
					Log.e("opera", operaString);
					sqliteHelper.delete_single_record(operaString);

					SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), // 设置绘图listview
															  get_History(), R.layout.item_list_bookmark, new String[] { "网页", "网址" },
															  new int[] { R.id.website_name, R.id.website_url });
					history_listview.setAdapter(adapter); // 更新绘制*/
					break;
				default:
					break;
			}
			return super.onContextItemSelected(item);

		}

		/* 取得数据库中的书签 */
		public ArrayList<HashMap<String, Object>> get_History() {
			SQLiteDatabase db = sqliteHelper.getWritableDatabase();
			myCursor = db.query(sqliteHelper.TB__HISTORY_NAME, new String[] { "name", "url" }, "isbookmark=1", null, null,
								null, "time" + " DESC");
			int url = myCursor.getColumnIndex("url");
			int name = myCursor.getColumnIndex("name");
			history_data_list.clear();
			if (myCursor.moveToFirst()) {
				do {
					HashMap<String, Object> item = new HashMap<String, Object>();
					item.put("网页", myCursor.getString(name));
					item.put("网址", myCursor.getString(url));
					history_data_list.add(item);
				} while (myCursor.moveToNext());
			}
			myCursor.close();
			return history_data_list;
		}

	}
	
