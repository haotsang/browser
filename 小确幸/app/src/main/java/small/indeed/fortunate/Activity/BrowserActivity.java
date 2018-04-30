package small.indeed.fortunate.Activity;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.Bitmap.*;
import android.graphics.drawable.*;
import android.net.*;
import android.net.http.*;
import android.os.*;
import android.preference.*;
import android.support.design.*;
import android.support.design.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.text.*;
import android.text.method.*;
import android.view.*;
import android.view.animation.*;
import android.webkit.*;
import android.webkit.WebSettings.*;
import android.widget.*;
import android.widget.TextView.*;
import java.util.*;
import java.util.regex.*;
import small.indeed.fortunate.Unit.*;
import small.indeed.fortunate.View.*;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.ClipboardManager;
import android.widget.LinearLayout.*;
import android.support.v7.widget.helper.*;
import javax.crypto.*;
import android.support.v7.view.menu.*;
import android.support.v4.app.*;
import android.print.*;
import java.io.*;

public class BrowserActivity extends AppCompatActivity {
	
	public static final int REQUEST_MENU_RESULT = 1;
	public static int ContextHeight;
	
	private CoordinatorLayout frameLayout;
	private FrameLayout contentFrame;
	private AgentWeb agentWeb;
	private FinderBar finderBar;
	
	private RelativeLayout omnibox;
	private ImageButton omniboxSafe;
	private TextView omniboxUrl;
	private ImageButton omniboxRefresh;
	private ImageButton omniboxTabswitcher;
	private ImageButton omniboxOverflow;
    private ProgressBar progressBar;
	
	private View customView;
    private VideoView videoView;
    private int originalOrientation;
    private WebChromeClient.CustomViewCallback customViewCallback;
	private ValueCallback<Uri[]> filePathCallback = null;
	
	String sourceEvent;
	Handler handlerks = new MyHandler();
	
	private ItemTouchHelper itemTouchHelper;
    private ItemTouchHelper.Callback itemTouchCallBack;
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		init();
		initWebView();
		agentWeb.addJavascriptInterface(this.new InJavaScriptLocalObj(), "java_obj");
    }
	
	final class InJavaScriptLocalObj {  //获取网页内容
		@JavascriptInterface
		public void getSource(final String var1) {
					Message message = Message.obtain();
					message.obj = var1;
					handlerks.sendMessage(message);
		}
		@JavascriptInterface
        public void showSource(String html) {
        }
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
		AgentWeb.destroyWebView(this.agentWeb);
    }
	
	@Override
	public void onBackPressed() {
		if (finderBar.getVisibility() == 0) {
            finderBar.closeFinderBar();
			agentWeb.clearMatches();
			omnibox.setVisibility(View.VISIBLE);
        } else if (agentWeb.canGoBack()) {
			agentWeb.goBack();
		} else {
			super.onBackPressed();
		}
	}
	
	class MyHandler extends Handler {
		@Override
        public void handleMessage(Message var1) {
            super.handleMessage(var1);

			if (sourceEvent.equals("网页源码")) {
				String var13 = (String)var1.obj;
				if (var13.length() >= 1) {
					agentWeb.loadDataWithBaseURL((String)null, "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"><title>网页源码</title><h3><style>xmp{word-wrap:break-word;white-space:pre-line;}</style><xmp>" + var13 + "</xmp>", "text/html", "utf-8", (String)null);
					agentWeb.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
					Snackbar.make(frameLayout, "网页源码", Snackbar.LENGTH_SHORT).show();
					return;
				} else {
					Snackbar.make(frameLayout, "没有源码", Snackbar.LENGTH_SHORT).show();
				}
			} else if (sourceEvent.equals("资源链接")) {
				String var3 = (String)var1.obj;
				Matcher var4 = Pattern.compile("(\"http:|\"https:)([^>]*?)\"").matcher(var3);
				ArrayList var5 = new ArrayList();

				while(var4.find()) {
					var5.add(var4.group());
				}

				StringBuilder var6 = new StringBuilder();
				Iterator var7 = ((Collection)var5).iterator();

				while(var7.hasNext()) {
					String var8 = (String)var7.next();
					String var9 = var8.substring(1, -1 + var8.length());
					if(var9.length() >= 10) {
						var6.append("<form style=\"margin:0 0 10px\">" + "<a href=\"" + var9 + "\">" + var9 + "</a>" + "</form>");
					}
				}

				String var11 = var6.toString();
				if(var11.length() < 1) {
					Snackbar.make(frameLayout, "没有资源", Snackbar.LENGTH_SHORT).show();
					return;
				}

				agentWeb.loadDataWithBaseURL((String)null, "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"><title>资源链接</title><h3><style>a{word-wrap:break-word;white-space:pre-line;}</style>" + var11, "text/html", "utf-8", (String)null);
				agentWeb.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
				Snackbar.make(frameLayout, "资源链接", Snackbar.LENGTH_SHORT).show();
				return;
			} else if(sourceEvent.equals("阅读模式")) {
				String var2 = BrowserUnit.HtmlText((String)var1.obj).replaceAll("\\s*?\\s+", "\n\n").trim();
				if(var2.length() < 1) {
					Snackbar.make(frameLayout, "没有文本", Snackbar.LENGTH_SHORT).show();
					return;
				}

				agentWeb.loadDataWithBaseURL((String)null, "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"><title>阅读模式</title><h3><style>xmp{word-wrap:break-word;white-space:pre-line;}</style><xmp>" + var2 + "</xmp>", "text/html", "utf-8", (String)null);
				agentWeb.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
				Snackbar.make(frameLayout, "阅读模式", Snackbar.LENGTH_SHORT).show();
				return;
			}
		}

	}
	
	
	private void getSource() {
		//agentWeb.loadUrl("javascript:window.local_obj.showSource" +  "('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");  
		agentWeb.loadUrl("javascript:window.java_obj.getSource(\'<head>\'+" + "document.getElementsByTagName(\'html\')[0].innerHTML+\'</head>\');");
	}
	
	
//	private void setStatusBarColor() {
//		if(Build.VERSION.SDK_INT >= 21) {
//			Bitmap var1 = BrowserUnit.getBitmapFromView(this.agentWeb);
//			if(var1 != null) {
//				int var2 = var1.getPixel(this.getWindowManager().getDefaultDisplay().getWidth() / 2, 10);
//				int var3 = Color.red(var2);
//				int var4 = Color.green(var2);
//				int var5 = Color.blue(var2);
//				if(Build.VERSION.SDK_INT >= 23) {
//					if(var3 > 200 && var4 > 200 && var5 > 200) {
//						this.getWindow().getDecorView().setSystemUiVisibility(8192);
//					} else {
//						this.getWindow().getDecorView().setSystemUiVisibility(0);
//					}
//				}
//				BrowserUnit.setWindowStatusBarColor(this, var2);
//				var1.recycle();
//			}
//		}
//
//	}
	
	private void init() {
		
		frameLayout = (CoordinatorLayout) findViewById(R.id.main_framelayout);
		omnibox = (RelativeLayout) findViewById(R.id.main_omnibox);
		contentFrame = (FrameLayout) findViewById(R.id.main_content);
		agentWeb = new AgentWeb(this);
		finderBar = (FinderBar) findViewById(R.id.finder_bar);
		
		omniboxSafe = (ImageButton) findViewById(R.id.main_omnibox_safe);
		omniboxUrl = (TextView) findViewById(R.id.main_omnibox_url);
		omniboxRefresh = (ImageButton) findViewById(R.id.main_omnibox_reload);
		omniboxTabswitcher = (ImageButton) findViewById(R.id.main_omnibox_tabswitcher);
		omniboxOverflow = (ImageButton) findViewById(R.id.main_omnibox_overflow);
		
		progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
		
		contentFrame.removeAllViews();
		contentFrame.addView(agentWeb);
		
		omniboxSafe.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
				}
		});
		
		omniboxUrl.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					View layout =  LayoutInflater.from(BrowserActivity.this).inflate(R.layout.dialog_input, null);
					final CustomDialog dialog = new CustomDialog(BrowserActivity.this);
					final EditTextExt inputBox = (EditTextExt) layout.findViewById(R.id.sheet_dialog_inputbox);
					final ImageButton inputClear = (ImageButton) layout.findViewById(R.id.sheet_dialog_inputclear);

					inputBox.setOnEditorActionListener(new OnEditorActionListener() {
							@Override
							public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
								String query = inputBox.getText().toString().trim();

								if (query.isEmpty()) {
									return true;
								}
								if (BrowserUnit.isURL(query)) {
									agentWeb.loadUrl(query);
								} else {
									SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BrowserActivity.this);
									int i = Integer.valueOf(sp.getString(getString(R.string.sp_search_engine), "0"));
									if (i == 0) {
										agentWeb.loadUrl(BrowserUnit.SEARCH_ENGINE_BING + query);
									} else if (i == 1) {
										agentWeb.loadUrl(BrowserUnit.SEARCH_ENGINE_BAIDU + query);
									}
								}
								BrowserUnit.hideSoftInput(BrowserActivity.this, inputBox);
								dialog.dismiss();
								return false;
							}
						});

					inputBox.addTextChangedListener(new TextWatcher() {
							@Override
							public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
							@Override
							public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
							@Override
							public void afterTextChanged(Editable arg0) {
								if (arg0.toString().equals("")) {
									inputClear.setVisibility(View.GONE);
								} else {
									inputClear.setVisibility(View.VISIBLE);
								}
							}}
					);

					inputClear.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								inputBox.setText(null);
							}
						});

					dialog.setContentView(layout);
					dialog.show();
					inputBox.setText(agentWeb.getUrl());
					BrowserUnit.showSoftInput(BrowserActivity.this, inputBox);
					
				}
			});
		
		omniboxRefresh.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (agentWeb.getProgress() == 100) {
						agentWeb.reload();
					} else {
						agentWeb.stopLoading();
					}
				}
			});
		
		omniboxTabswitcher.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final BottomSheetDialog dialog = new BottomSheetDialog(BrowserActivity.this);
					dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);  //←重点在这里，来，都记下笔记
					View contentView = View.inflate(BrowserActivity.this, R.layout.sheet_dialog_tabswitcher, null);
					final RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.my_recycler_view);
					
					final LinearLayoutManager layoutManager = new LinearLayoutManager(BrowserActivity.this );  
					layoutManager.setOrientation(OrientationHelper. VERTICAL);
					recyclerView.setLayoutManager(layoutManager);
					recyclerView.addItemDecoration( new MyDividerItemDecoration(BrowserActivity.this, LinearLayoutManager.VERTICAL));  
					recyclerView.setItemAnimator( new DefaultItemAnimator());  
					
					final MyRecyclerAdapter recycleAdapter = new MyRecyclerAdapter(getData());
					
					itemTouchCallBack = new ItemTouchHelper.Callback() {
						/**
						 * 设置滑动类型标记
						 *
						 * @param recyclerView
						 * @param viewHolder
						 * @return
						 *          返回一个整数类型的标识，用于判断Item那种移动行为是允许的
						 */
						@Override
						public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
							return makeMovementFlags(0,ItemTouchHelper.START | ItemTouchHelper.END);
						}

						/**
						 * Item是否支持长按拖动
						 *
						 * @return
						 *          true  支持长按操作
						 *          false 不支持长按操作
						 */
						@Override
						public boolean isLongPressDragEnabled() {
							return false;
						}

						/**
						 * Item是否支持滑动
						 *
						 * @return
						 *          true  支持滑动操作
						 *          false 不支持滑动操作
						 */
						@Override
						public boolean isItemViewSwipeEnabled() {
							return true;
						}

						/**
						 * 拖拽切换Item的回调
						 *
						 * @param recyclerView
						 * @param viewHolder
						 * @param target
						 * @return
						 *          如果Item切换了位置，返回true；反之，返回false
						 */
						@Override
						public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
							return true;
						}

						/**
						 * 滑动删除Item
						 *
						 * @param viewHolder
						 * @param direction
						 *           Item滑动的方向
						 */
						@Override
						public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
							recycleAdapter.delete(viewHolder.getAdapterPosition());
						}

						/**
						 * Item被选中时候回调
						 *
						 * @param viewHolder
						 * @param actionState
						 *          当前Item的状态
						 *          ItemTouchHelper.ACTION_STATE_IDLE   闲置状态
						 *          ItemTouchHelper.ACTION_STATE_SWIPE  滑动中状态
						 *          ItemTouchHelper#ACTION_STATE_DRAG   拖拽中状态
						 */
						@Override
						public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
							//  item被选中的操作
							if(actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
								viewHolder.itemView.setBackgroundResource(R.color.accent);
							}
							super.onSelectedChanged(viewHolder, actionState);
						}

						/**
						 * 移动过程中绘制Item
						 *
						 * @param c
						 * @param recyclerView
						 * @param viewHolder
						 * @param dX
						 *          X轴移动的距离
						 * @param dY
						 *          Y轴移动的距离
						 * @param actionState
						 *          当前Item的状态
						 * @param isCurrentlyActive
						 *          如果当前被用户操作为true，反之为false
						 */
						@Override
						public void onChildDraw(Canvas c, RecyclerView recyclerView,
												RecyclerView.ViewHolder viewHolder,
												float dX, float dY, int actionState, boolean isCurrentlyActive) {
							float x = Math.abs(dX) + 0.5f;
							float width = viewHolder.itemView.getWidth();
							float alpha = 1f - x / width;
							viewHolder.itemView.setAlpha(alpha);
							super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState,
											  isCurrentlyActive);
						}

						/**
						 * 移动过程中绘制Item
						 *
						 * @param c
						 * @param recyclerView
						 * @param viewHolder
						 * @param dX
						 *          X轴移动的距离
						 * @param dY
						 *          Y轴移动的距离
						 * @param actionState
						 *          当前Item的状态
						 * @param isCurrentlyActive
						 *          如果当前被用户操作为true，反之为false
						 */
						@Override
						public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
													RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState,
													boolean isCurrentlyActive) {
							super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState,
												  isCurrentlyActive);
						}

						/**
						 * 用户操作完毕或者动画完毕后会被调用
						 *
						 * @param recyclerView
						 * @param viewHolder
						 */
						@Override
						public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
							// 操作完毕后恢复颜色
							viewHolder.itemView.setBackgroundResource(android.R.color.white);
							viewHolder.itemView.setAlpha(1.0f);
							super.clearView(recyclerView, viewHolder);
						}
					};
					
					
					
					itemTouchHelper = new ItemTouchHelper(itemTouchCallBack);
					itemTouchHelper.attachToRecyclerView(recyclerView);
					
					
					recyclerView.setAdapter(recycleAdapter);
					
					recycleAdapter.setOnItemClickListener(new MyRecyclerAdapter.OnItemClickListener() {
							@Override
							public void onItemClick(View view, int position) {
								Toast.makeText(BrowserActivity.this,"click " + position + " item", Toast.LENGTH_SHORT).show();
							}
							@Override
							public void onItemLongClick(View view, int position) {
								Toast.makeText(BrowserActivity.this,"long click " + position + " item", Toast.LENGTH_SHORT).show();
							}
						});
					
					final ImageButton button1 = (ImageButton) contentView.findViewById(R.id.sheet_dialog_tabswitcherImageButton1);
					final ImageButton button2 = (ImageButton) contentView.findViewById(R.id.sheet_dialog_tabswitcherImageButton2);
					
					button1.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								recycleAdapter.addNewItem();
								// 由于Adapter内部是直接在首个Item位置做增加操作，增加完毕后列表移动到首个Item位置
								layoutManager.scrollToPosition(0);
							}
					});
					button2.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								recycleAdapter.deleteItem();
								// 由于Adapter内部是直接在首个Item位置做删除操作，删除完毕后列表移动到首个Item位置
								layoutManager.scrollToPosition(0);
							}
						});

					dialog.setContentView(contentView);
					dialog.show();
				}
		});
			
		omniboxOverflow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {	
					View layout =  LayoutInflater.from(BrowserActivity.this).inflate(R.layout.g, null);
					ListView listView = layout.findViewById(R.id.layout_resListView);

					String[] items = new String[] { "返回上一页", "分享", "书签&历史", "下载管理", "电脑模式", "页内查找", "添加到桌面", "截取网页", "保存为PDF", "翻译网页", "查看源码", "网页资源", "阅读模式", "设置" };

					ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(BrowserActivity.this,
																			 android.R.layout.simple_list_item_1,
																			 android.R.id.text1,
																			 new ArrayList<String>(Arrays.asList(items)));
					listView.setAdapter(mAdapter);
					listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
								PopWindowUtil.getInstance().dismissWindow();
								switch (position) {
									case 0:
										if (agentWeb.canGoForward()) {
											agentWeb.goForward();
										}
										break;
									case 1:
										try {
											IntentUnit.share(BrowserActivity.this, agentWeb.getUrl());
										} catch (Exception e) {}
										break;
									case 2:
										startActivity(new Intent(BrowserActivity.this, MarkHistoryActivity.class));
										break;
									case 3:
										startActivity(new Intent(BrowserActivity.this, DownloadActivity.class));
										break;
									case 4:
										
										break;
									case 5:
										pageFinder();
										omnibox.setVisibility(View.GONE);
										break;
									case 6:
										try {
											Intent shortcutIntent = new Intent(BrowserActivity.this, BrowserActivity.class);
											shortcutIntent.setData(Uri.parse(agentWeb.getUrl()));
											shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);// 加入action,和category之后，程序卸载的时候才会主动将该快捷方式也卸载

											Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
											addIntent.putExtra("duplicate", false);	// 不重复创建快捷方式图标
											addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
											addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, agentWeb.getTitle());
											addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, agentWeb.getFavicon());
											sendBroadcast(addIntent);
											ToastUtil.show(BrowserActivity.this, R.string.toast_successful);
										} catch (Exception e) {
											ToastUtil.show(BrowserActivity.this, R.string.toast_failed);
										}
										break;
									case 7:
										Picture picture = agentWeb.capturePicture();
										Bitmap b = Bitmap.createBitmap(
											picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
										Canvas c = new Canvas(b);
										picture.draw(c);
										File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "page.jpg");
										if(file.exists()){
											file.delete();
										}
										FileOutputStream fos = null;
										try {
											fos = new FileOutputStream(file.getAbsoluteFile());
											if (fos != null) {
												b.compress(Bitmap.CompressFormat.JPEG, 90, fos);
												fos.close();
												Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
											}
										} catch (Exception e) {
											e.printStackTrace();
											Toast.makeText(getApplicationContext(), "保存失败", Toast.LENGTH_SHORT).show();
										}
										break;
									case 8:
										if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
											PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
											PrintDocumentAdapter printAdapter = agentWeb.createPrintDocumentAdapter();
											String jobName = getString(R.string.app_name) + " Document";
											printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
										} else {
											Toast.makeText(getApplicationContext(), "当前系统不支持该功能", Toast.LENGTH_SHORT).show();
										}
										break;
									case 9:
										agentWeb.loadUrl(new StringBuffer().append(new StringBuffer().append("http://translate.baiducontent.com/transpage?query=").append(agentWeb.getUrl()).toString()).append("&from=auto&to=en&source=url").toString());
										break;
									case 10:
										sourceEvent = "网页源码";
										getSource();
										break;
									case 11:
										sourceEvent = "资源链接";
										getSource();
										break;
									case 12:
										sourceEvent = "阅读模式";
										getSource();
										break;
									case 13:
										startActivity(new Intent(BrowserActivity.this, SettingActivity.class));
										break;
									default:
										break;
								}
							}
						});

					PopWindowUtil.getInstance().makePopupWindow(BrowserActivity.this, omniboxOverflow, layout)
					.showLocationWithAnimation(BrowserActivity.this, omniboxOverflow, 0, 0, R.style.PopupAnimation);
				}
		});
		
		
		if (BrowserUnit.isURL(ClipboardUrl())) {
			Snackbar.make(frameLayout, ClipboardUrl(), Snackbar.LENGTH_LONG).setAction("打开", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						agentWeb.loadUrl(ClipboardUrl());
					}
				}).show();
		}
		
		finderBar.setFinderListener(new FinderBar.FinderListener() {
				@Override
				public void onNextClick() {
					agentWeb.findNext(true);
				}
				@Override
				public void onPreClick() {
					agentWeb.findNext(false);
				}
				@Override
				public void onFindKeyChanged(CharSequence s) {
					agentWeb.findAllAsync(s.toString().trim());
				}
				@Override
				public void onClosed() {
					getWindow().setSoftInputMode(48);
					BrowserUnit.hideSoftInput(BrowserActivity.this);
					agentWeb.clearMatches();
					omnibox.setVisibility(View.VISIBLE);
				}
			});
        agentWeb.setFindListener(new AgentWeb.FindListener() {
				public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
					finderBar.setMatchesNumber(activeMatchOrdinal + REQUEST_MENU_RESULT, numberOfMatches);
				}
			});
			
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			if (getIntent().getData() != null) {
				this.agentWeb.loadUrl(getIntent().getData().toString());
			}
		} else if (Intent.ACTION_WEB_SEARCH.equals(getIntent().getAction())) {
			if (getIntent().getStringExtra("query").length() > 0) {
				this.agentWeb.loadUrl(BrowserUnit.SEARCH_ENGINE_BAIDU + getIntent().getStringExtra("query"));
			} else if (BrowserUnit.isURL(getIntent().getStringExtra("query"))) {
				this.agentWeb.loadUrl(getIntent().getStringExtra("query"));
			}
		} else {
			this.agentWeb.loadUrl("http://m.sogou.com");
		}
				
	}
	
	
	private void pageFinder() {
        getWindow().setSoftInputMode(16);
        finderBar.setVisibility(0);
		
        new Handler().postDelayed(new Runnable() {
				public void run() {
					BrowserUnit.showSoftInput(BrowserActivity.this, finderBar.getEtKeyWords());
				}
			}, 60);
    }
	
	private ArrayList<String> getData() {
        ArrayList<String> data = new ArrayList<>();
        String temp = agentWeb.getTitle().toString();
        for(int i = 1; i < 10; i++) {
            data.add(i + temp);
        }
        return data;
    }
	
	private String ClipboardUrl() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService("clipboard");
        String str = "";
        if (clipboardManager != null) {
            try {
                if (clipboardManager.hasText()) {
                    CharSequence text = clipboardManager.getText();
                    clipboardManager.setText(text);
                    if (text != null && text.length() > 0) {
                        return text.toString().trim();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
        return str;
    }
	
	private void initWebView() {
		agentWeb.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					if (url.contains("baidu.com/wap/link") || url.contains("baidu.com/wap/shareview") || url.contains("baidu.com/s/") || url.contains("baidu.com/wap/view")) {
						agentWeb.getSettings().setUserAgentString(BrowserUnit.UA_SYMBIAN);
					} else {
						agentWeb.getSettings().setUserAgentString(agentWeb.getSettings().getUserAgentString());
					}
				}
				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					BrowserUnit.setStatusBarColor(BrowserActivity.this, view);
				}
				@Override
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
					super.onReceivedError(view, errorCode, description, failingUrl);
					ToastUtil.show(BrowserActivity.this, description);
				}
				@Override
				public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
					super.doUpdateVisitedHistory(view, url, isReload);
				}
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, final String url) {
					if (url.startsWith(WebView.SCHEME_TEL) || url.startsWith(WebView.SCHEME_GEO) || url.startsWith(WebView.SCHEME_MAILTO)) {
						try {
							Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
							startActivity(intent);
							return true;
						} catch (Exception e) {
							ToastUtil.show(BrowserActivity.this, R.string.toast_intent_failed);
						}
					}
					return super.shouldOverrideUrlLoading(view, url);
				}
				@Deprecated
				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
					return super.shouldInterceptRequest(view, url);
				}
				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
					return super.shouldInterceptRequest(view, request);
				}
				@Override
				public void onFormResubmission(WebView view, final Message dontResend, final Message resend) {
					AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
					builder.setCancelable(false);
					builder.setTitle(R.string.dialog_title_resubmission);
					builder.setMessage(R.string.dialog_content_resubmission);
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								resend.sendToTarget();
							}
						});
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dontResend.sendToTarget();
							}
						});
					builder.create().show();
				}
				@Override
				public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
					AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
					builder.setCancelable(false);
					builder.setTitle(R.string.dialog_title_warning);
					builder.setMessage(R.string.dialog_content_ssl_error);
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								handler.proceed();
							}
						});
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								handler.cancel();
							}
						});
					AlertDialog dialog = builder.create();
					if (error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
						dialog.show();
					} else {
						handler.proceed();
					}
				}
				@Override
				public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, String host, String realm) {
					AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
					builder.setCancelable(false);
					builder.setTitle(R.string.dialog_title_sign_in);
					LinearLayout signInLayout = (LinearLayout) LayoutInflater.from(BrowserActivity.this).inflate(R.layout.dialog_sign_in, null, false);
					final EditText userEdit = (EditText) signInLayout.findViewById(R.id.dialog_sign_in_username);
					final EditText passEdit = (EditText) signInLayout.findViewById(R.id.dialog_sign_in_password);
					passEdit.setTypeface(Typeface.DEFAULT);
					passEdit.setTransformationMethod(new PasswordTransformationMethod());
					builder.setView(signInLayout);
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String user = userEdit.getText().toString().trim();
								String pass = passEdit.getText().toString().trim();
								handler.proceed(user, pass);
							}
						});
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								handler.cancel();
							}
						});
					builder.create().show();
				}
			});

		agentWeb.setWebChromeClient(new WebChromeClient() {
				@Override
				public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
//					Message href = view.getHandler().obtainMessage();
//					view.requestFocusNodeHref(href);
//					String url = href.getData().getString("url");
//					
//					Intent intent = new Intent(BrowserActivity.this, BrowserActivity.class);
//					intent.setData(Uri.parse(url));
//					intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//					startActivity(intent, ActivityOptions.makeBasic().toBundle());
					
					WebView newWebView = new WebView(view.getContext());  
					newWebView.setWebViewClient(new WebViewClient() {  
							@Override  
							public boolean shouldOverrideUrlLoading(WebView view, String url) {
//								Intent intent = new Intent(BrowserActivity.this, BrowserActivity.class);
//								intent.setData(Uri.parse(url));
//								intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//								startActivity(intent, ActivityOptions.makeBasic().toBundle());
								
								Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));  
								startActivity(browserIntent);  
								return true;  
							}  
						});  
					WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;  
					transport.setWebView(newWebView);  
					resultMsg.sendToTarget();  
					
					return true;
				}
				@Override  
				public void onCloseWindow(WebView window) {//html中，用js调用.close(),会回调此函数  
					super.onCloseWindow(window);
					AgentWeb.destroyWebView(window);
				}
				
				
				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					super.onProgressChanged(view, newProgress);
					//			        //平滑的加载进度
					if (newProgress < 2) {
						progressBar.setProgress(2);
					} else if (newProgress > progressBar.getProgress()) {
						ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", newProgress);
						//animator.setStartDelay(getResources().getInteger(android.R.integer.config_longAnimTime));
						animator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
						animator.setInterpolator(new DecelerateInterpolator());
						animator.start();
					} else {
						progressBar.setProgress(newProgress);
					}

					if (newProgress == 100) {
						//进度条优雅的退场
						ObjectAnimator outAni = ObjectAnimator.ofFloat(progressBar, "alpha", 1, 0);
						outAni.setStartDelay(getResources().getInteger(android.R.integer.config_longAnimTime));
						outAni.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
						outAni.addListener(new Animator.AnimatorListener() {
								@Override
								public void onAnimationStart(Animator animation) {}
								@Override
								public void onAnimationEnd(Animator animation) {
									progressBar.setVisibility(View.GONE);
									progressBar.setAlpha(1);
									omniboxRefresh.setImageResource(R.drawable.ic_reload);
								}
								@Override
								public void onAnimationCancel(Animator animation) {}
								@Override
								public void onAnimationRepeat(Animator animation) {}
							});
						outAni.start();
					} else {
						//进度条加载时始终使其可见
						progressBar.setVisibility(View.VISIBLE);
						omniboxRefresh.setImageResource(R.drawable.ic_stop);
					}
				}
				@Override
				public void onReceivedTitle(WebView view, String title) {
					super.onReceivedTitle(view, title);
					omniboxUrl.setText(view.getUrl());
				}
				@Override
				public void onReceivedIcon(WebView view, Bitmap icon) {
					super.onReceivedIcon(view, icon);
				}//imageView.setImageBitmap(icon);
				@Deprecated
				@Override
				public void onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback) {
					super.onShowCustomView(view, requestedOrientation, callback);
					onShowCustomView(view, requestedOrientation, callback);
				}
				@Override
				public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
					super.onShowCustomView(view, callback);
					if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					}
				}
				@Override
				public void onHideCustomView() {
					super.onHideCustomView();
					if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					}
				}
				@Override
				public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
					super.onGeolocationPermissionsShowPrompt(origin, callback);
					AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
					builder.setCancelable(false);
					builder.setMessage("允许网页访问地理位置");
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								callback.invoke(origin, true, true);
							}
						});
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								callback.invoke(origin, false, true);
							}
						});
					builder.create().show();
				}
				//设置响应js 的Alert()函数
				@Override
				public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
					AlertDialog.Builder b = new AlertDialog.Builder(BrowserActivity.this);
					b.setMessage(message);
					b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						});
					b.setCancelable(false);
					b.create().show();
					return true;
				}
				//设置响应js 的Confirm()函数
				@Override
				public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
					AlertDialog.Builder b = new AlertDialog.Builder(BrowserActivity.this);
					b.setMessage(message);
					b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						});
					b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.cancel();
							}
						});
					b.setCancelable(false);
					b.create().show();
					return true;
				}
				//设置响应js 的Prompt()函数
				@Override
				public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
					View v =  LayoutInflater.from(BrowserActivity.this).inflate(R.layout.dialog_edit, null);
					final EditText editText = (EditText) v.findViewById(R.id.layout_dialog_edit_text);
					editText.setText(defaultValue);
					AlertDialog.Builder b = new AlertDialog.Builder(BrowserActivity.this);
					b.setMessage(message);
					b.setView(v);
					b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String value = editText.getText().toString().trim();
								result.confirm(value);
							}
						});
					b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.cancel();
							}
						});
					b.setCancelable(false);
					b.create().show();
					return true;
				}
				// For Android 5.0+
				@Override
				public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						filePathCallback = valueCallback;
						try {
							Intent intent = fileChooserParams.createIntent();
							startActivityForResult(intent, IntentUnit.REQUEST_FILE_21);
						} catch (Exception e) {
							ToastUtil.show(BrowserActivity.this, R.string.toast_open_file_manager_failed);
						}
					}
					return true;
				}
			});

		agentWeb.setDownloadListener(new DownloadListener() {
				@Override
				public void onDownloadStart(final String url, String userAgent, final String contentDisposition, final String mimeType, long contentLength) {
					if (BrowserUnit.hasApp(BrowserActivity.this, "com.dv.adm.pay")) {
						BrowserUnit.downloadByADM(BrowserActivity.this, url, mimeType);
					} else {
						Snackbar.make(frameLayout, contentDisposition, Snackbar.LENGTH_INDEFINITE).setAction("下载", new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									BrowserUnit.download(BrowserActivity.this, url, contentDisposition, mimeType);
								}
							}).show();
					}
				}
			});

		agentWeb.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					final WebView.HitTestResult result = agentWeb.getHitTestResult();
					final String target = result.getExtra();

					final ArrayList<String> list = new ArrayList<String>();
					list.add(0, getString(R.string.dialog_longpress_new_tab));
					list.add(1, getString(R.string.dialog_longpress_copy_link));
					if (result != null && (result.getType() == WebView.HitTestResult.IMAGE_TYPE || result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
						list.add(2, getString(R.string.dialog_longpress_find_img));
						list.add(3, getString(R.string.dialog_longpress_save_img));
					}
					
					AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
					builder.setTitle(target);
					builder.setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
									case 0:
										
										break;
									case 1:
										BrowserUnit.copyURL(BrowserActivity.this, target);
										break;
									case 2:
										String findimg = new StringBuffer().append("http://pic.sogou.com/pic/ris_searchList.jsp?statref=home&v=5&ul=1&keyword=").append(target).toString();
										agentWeb.loadUrl(findimg);
										break;
									case 3:
										if (BrowserUnit.hasApp(BrowserActivity.this, "com.dv.adm.pay")) {
											BrowserUnit.downloadByADM(BrowserActivity.this, target, BrowserUnit.MIME_TYPE_IMAGE);
										} else {
											Snackbar.make(frameLayout, target, Snackbar.LENGTH_INDEFINITE).setAction("下载", new View.OnClickListener() {
													@Override
													public void onClick(View v) {
														BrowserUnit.download(BrowserActivity.this, target, target, BrowserUnit.MIME_TYPE_IMAGE);
													}
												}).show();
										}
										break;
									default:
										break;
								}
							}
						});

					if (target != null || (result != null && result.getExtra() != null)) {
						if (result == null) {
							result.getExtra();
						}
						AlertDialog alertDialog = builder.create();
						alertDialog.show();
					}

					return false;
				}
			});
		
			
		agentWeb.setOnTouchListener(new View.OnTouchListener() {
				float location = 0;
				float y = 0;
				int action = 0;
				@Override
				public boolean onTouch(View view, MotionEvent arg1) {
					if (view != null && !view.hasFocus()) {
						view.requestFocus();
					}
					action = arg1.getAction();
					y = arg1.getY();
					if (action == MotionEvent.ACTION_DOWN) {
						location = y;
					} else if (action == MotionEvent.ACTION_UP) {
						if ((y - location) > 20) {
							if (agentWeb.getScrollY() < 5 && omnibox.isShown()) {
								//omnibox.setVisibility(View.GONE);
							} else {
								//omnibox.setVisibility(View.VISIBLE);
							}
						} else if ((y - location) < -20) {
							//omnibox.setVisibility(View.GONE);
						}
						location = 0;
					}

					return false;
				}

			});
		
	}
	
	
	
}
