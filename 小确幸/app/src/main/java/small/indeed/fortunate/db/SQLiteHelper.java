package small.indeed.fortunate.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {

	public static String DB_NAME = "History.db";// 数据库名称；处理为：所有的都记为记录，添加bookmark的标签，为true时则为书签，否则则是普通的历史记录
	public String TB__HISTORY_NAME = "allHistory";// 表名-历史记录和书签
	private static SQLiteHelper instance = null;   //数据库对象
	private Cursor temp_cursor;   //查询

	public static SQLiteHelper getInstance(Context context) {   //传入上下文对象获得实例
		if (instance == null) {   //如果为空
			instance = new SQLiteHelper(context);    //默认赋值
		}
		return instance;
	}

	public SQLiteHelper(Context context) {   //构造方法,传入上下文
		super(context, DB_NAME, null, 1);    //传入名称,上下文,数据库版本
	}

	@Override
	public void onCreate(SQLiteDatabase db) {    //调用数据库对象时会自动进入该方法
		// 创建默认的allHistory表   历史记录
		db.execSQL("create table " + TB__HISTORY_NAME + " ( "    //执行命令
				   + "name" + " varchar, " + "url"
				   + " varchar, " + "isbookmark" + " integer, "
				   + "time" + " integer)");
	}

	/*
	 * public void createTable(SQLiteDatabase db, String table) {
	 * db.execSQL("create table " + table + " ( " + HistoryBean.NAME +
	 * " varchar, " + HistoryBean.URL + " varchar, " + HistoryBean.ISBOOKMARK +
	 * " integer, " + HistoryBean.TIME + " integer)"); }
	 */

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		/**
		 37          * 1、第一次创建数据库的时候，这个方法不会走
		 38          * 2、清除数据后再次运行(相当于第一次创建)这个方法不会走
		 39          * 3、数据库已经存在，而且版本升高的时候，这个方法才会调用
		 40          */
	}

	// 添加历史记录--isbookmark=0表示非书签，普通历史记录；bookmark=1表示书签
	public void add_history(Context context, String name, String url,
							int isbookmark) {
		String SQL = null;
		String TIP = null;
		int time = (int) Math.floor(System.currentTimeMillis() / 1000);  //取得当前时间
		SQLiteDatabase db = this.getWritableDatabase();   //创建或打开数据库
		temp_cursor = db.rawQuery("select * from " + TB__HISTORY_NAME    //查询
								  + " where name=" + "'" + name + "';", null);
		if (temp_cursor.moveToFirst()) {   //判断是否为空
			SQL = "update " + TB__HISTORY_NAME + " set " + "time"   //更新值
				+ "=" + time + "," + "isbookmark" + "="
				+ isbookmark + " where name=" + "'" + name + "';";
			TIP = "update";
		} else {
			// 疑问：关于整型引号问题
			SQL = "insert into  " + TB__HISTORY_NAME + "(" + "time"    //插入
				+ "," + "name" + "," + "url" + ","
				+ "isbookmark" + ")" + "values(" + time + ",'"
				+ name + "','" + url + "'," + isbookmark + ");";
			TIP = "insert";
		}
		try {
			db.execSQL(SQL);      //执行命令
			//Toast.makeText(context, TIP + "了记录", Toast.LENGTH_LONG).show();
			Log.e("sqlite", TIP + "了记录");
		} catch (SQLException e) {
			//Toast.makeText(context, TIP + "记录出错", Toast.LENGTH_LONG).show();
			Log.e("splite", TIP + "了记录");
			return;
		}
	}
	public void delete_single_record(String name){    //删除
		String SQL = "delete from " + TB__HISTORY_NAME + " where name=" +"'" + name + "'";
		SQLiteDatabase dbHelper = this.getWritableDatabase();  //创建或打开数据库
		try{
			dbHelper.execSQL(SQL);   //执行命令
			Log.e("delete_single_record", "success");
		}catch(Exception e){
			Log.e("delete_single_record", "failed");
		}
	}


}
