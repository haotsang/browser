package small.indeed.fortunate.ui.activities;

import android.animation.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.net.http.*;
import android.os.*;
import android.preference.*;
import android.print.*;
import android.support.design.*;
import android.support.design.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.text.method.*;
import android.view.*;
import android.view.animation.*;
import android.webkit.*;
import android.webkit.WebSettings.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import small.indeed.fortunate.*;
import small.indeed.fortunate.db.*;
import small.indeed.fortunate.ui.activities.*;
import small.indeed.fortunate.utils.*;
import small.indeed.fortunate.view.*;

import android.support.design.R;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.widget.AdapterView.*;
import small.indeed.fortunate.ui.activities.BrowserActivity.*;
import android.content.res.*;


public class BrowserActivity extends AppCompatActivity {
	
	private CoordinatorLayout coordinatorLayout;
    private FrameLayout browserFrame;
	private FinderBar finderBar;
	private int REQUEST_MENU_RESULT = 1;
	
	private AgentWeb agentWeb;
	private ProgressBar progressBar;
	
	private RelativeLayout topLayout;
	private ImageButton topMenu;
	private TextView topUrl;
	private ImageButton topReload;
	
	private RelativeLayout bottomLayout;
	private ImageButton bottomBack;
	private ImageButton bottomForward;
	private ImageButton bottomHome;
	private ImageButton bottomTab;
	private ImageButton bottomMenu;

	private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
	private ValueCallback<Uri[]> filePathCallback = null;
	
	String sourceEvent;
	

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
		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();
		init();
		initWebView();
		initialize(savedInstanceState);
		agentWeb.addJavascriptInterface(new InJavaScriptLocalObj(), "java_obj");
    }

	private void initialize(Bundle savedInstanceState)
	{
		
		
	}
	
	final class InJavaScriptLocalObj {  //获取网页内容
		@JavascriptInterface
		public void showToast(String toast) {
			Toast.makeText(BrowserActivity.this, toast, Toast.LENGTH_SHORT).show();
		}
		@JavascriptInterface
		public void getSource(String var1) {
			Message message = Message.obtain();
			message.obj = var1;
			handler.sendMessage(message);
		}
		@JavascriptInterface
		public void showSource(String html) {
		}
		@JavascriptInterface  
		public void getHtmlSource(String html,String charactersets){ 
		}
	}  
	
	@Override
    protected void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
		dispatchIntent(getIntent());
    }
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
		dispatchIntent(intent);
    }
	@Override
	protected void onResume() { // 在oncreat后执行
		super.onResume();
		agentWeb.resume();
	}
	
	@Override
	protected void onPause() { // 按下暂停
		super.onPause();
		agentWeb.pause();
	}
	@Override
    protected void onDestroy() {
        super.onDestroy();
		handler.removeCallbacksAndMessages(null);
		agentWeb.destroy();
    }
	
	@Override
	public void onBackPressed() {
		if (finderBar.getVisibility() == View.VISIBLE) {
            finderBar.closeFinderBar();
			agentWeb.clearMatches();
        } else if (agentWeb.canGoBack()) {
			agentWeb.goBack();
		} else {
			super.onBackPressed();
		}
	}
	
	private void dispatchIntent(Intent intent) {
		String action = intent.getAction();
        if ("android.intent.action.VIEW".equals(action)) {
            Uri data = intent.getData();
            if (data != null) {
                pinAlbums(data.toString());
            }
        } else if (intent.getDataString() != null) {
			pinAlbums(intent.getDataString());
		} else if ("android.intent.action.WEB_SEARCH".equals(action)) {
            action = intent.getStringExtra("query");
            if (action.length() > 0) {
                pinAlbums(new StringBuffer().append("https://m.baidu.com/s?from=&wd=").append(action).toString());
            } else if (action.length() > 0 || URLUtil.isNetworkUrl(action)) {
				pinAlbums(action.toString());
			}
        } else {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            if (sp.getBoolean(getString(R.string.sp_first), true)) {
                String lang;
                if (getResources().getConfiguration().locale.getLanguage().equals("zh")) {
                    lang = BrowserUtil.INTRODUCTION_ZH;
                } else {
                    lang = BrowserUtil.INTRODUCTION_EN;
                }
                pinAlbums(BrowserUtil.BASE_URL + lang);
                sp.edit().putBoolean(getString(R.string.sp_first), false).commit();
            } else {
                pinAlbums(BrowserUtil.HOME_PAGE);
            }
        }
    }

	private void pinAlbums(String url) {
		ToastUtil.show(this, "new intent");
		agentWeb.loadUrl(url);
	}
	
	private Handler handler=new Handler() {
		@Override
        public void handleMessage(Message var1) {
            super.handleMessage(var1);

			if (sourceEvent.equals("网页源码")) {
				String var13 = (String)var1.obj;
				if (var13.length() >= 1) {
					agentWeb.loadDataWithBaseURL((String)null, "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"><title>网页源码</title><h3><style>xmp{word-wrap:break-word;white-space:pre-line;}</style><xmp>" + var13 + "</xmp>", "text/html", "utf-8", (String)null);
					agentWeb.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
					Snackbar.make(coordinatorLayout, "网页源码", Snackbar.LENGTH_SHORT).show();
					return;
				} else {
					Snackbar.make(coordinatorLayout, "没有源码", Snackbar.LENGTH_SHORT).show();
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
					Snackbar.make(coordinatorLayout, "没有资源", Snackbar.LENGTH_SHORT).show();
					return;
				}

				agentWeb.loadDataWithBaseURL((String)null, "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"><title>资源链接</title><h3><style>a{word-wrap:break-word;white-space:pre-line;}</style>" + var11, "text/html", "utf-8", (String)null);
				agentWeb.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
				Snackbar.make(coordinatorLayout, "资源链接", Snackbar.LENGTH_SHORT).show();
				return;
			} else if(sourceEvent.equals("阅读模式")) {
				String var2 = BrowserUtil.HtmlText((String)var1.obj).replaceAll("\\s*?\\s+", "\n\n").trim();
				if(var2.length() < 1) {
					Snackbar.make(coordinatorLayout, "没有文本", Snackbar.LENGTH_SHORT).show();
					return;
				}

				agentWeb.loadDataWithBaseURL((String)null, "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"><title>阅读模式</title><h3><style>xmp{word-wrap:break-word;white-space:pre-line;}</style><xmp>" + var2 + "</xmp>", "text/html", "utf-8", (String)null);
				agentWeb.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
				Snackbar.make(coordinatorLayout, "阅读模式", Snackbar.LENGTH_SHORT).show();
				return;
			}
		}

	};

	
	private void getSource() {
		//agentWeb.loadUrl("javascript:window.local_obj.showSource" +  "('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");  
		agentWeb.loadUrl("javascript:window.java_obj.getSource(\'<head>\'+" + "document.getElementsByTagName(\'html\')[0].innerHTML+\'</head>\');");
	}
	
	private void init() {
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
		browserFrame = (FrameLayout) findViewById(R.id.content_frame);
		finderBar = (FinderBar) findViewById(R.id.finder_bar);
		agentWeb = new AgentWeb(this);
		progressBar = (ProgressBar) findViewById(R.id.top_progress_bar);

		topLayout = (RelativeLayout) findViewById(R.id.top_bar_layout);
		topMenu = (ImageButton) findViewById(R.id.top_menu);
		topUrl = (TextView) findViewById(R.id.top_url);
		topReload = (ImageButton) findViewById(R.id.top_reload);

		bottomLayout = (RelativeLayout) findViewById(R.id.bottom_bar_layout);
		bottomBack = (ImageButton) findViewById(R.id.bottom_back);
		bottomForward = (ImageButton) findViewById(R.id.bottom_forward);
		bottomHome = (ImageButton) findViewById(R.id.bottom_home);
		bottomTab = (ImageButton) findViewById(R.id.bottom_multi);
		bottomMenu = (ImageButton) findViewById(R.id.bottom_menu);
		
		topUrl.setCompoundDrawablePadding(ViewUtil.dp22px(this, 5.0f));
		
		browserFrame.removeAllViews();
		browserFrame.addView(agentWeb);

		bottomBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (agentWeb.canGoBack()) {
						agentWeb.goBack();
					}
				}
			});
		bottomForward.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (agentWeb.canGoForward()) {
						agentWeb.goForward();
					}
				}
			});
		bottomHome.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					agentWeb.loadUrl(BrowserUtil.HOME_PAGE);
				}
			});
		bottomTab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
				}
			});
		bottomMenu.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final MyBottomSheetDialog dialog = new MyBottomSheetDialog(BrowserActivity.this);
					LinearLayout dialogView = (LinearLayout) LayoutInflater.from(BrowserActivity.this).inflate(R.layout.grid_view, null, false);
					
					GridView gridView = dialogView.findViewById(R.id.gridview);
					gridView.setAdapter(new MyGridAdapter());
					gridView.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view,
													int position, long id) {
								dialog.dismiss();
								switch (position) {
									case 0:
										startActivity(new Intent(BrowserActivity.this, BookmarkActivity.class));
										break;
									case 1:

										break;
									case 2:

										break;
									case 3:
										startActivity(new Intent(BrowserActivity.this, DownloadActivity.class));
										break;
									case 4:
										startActivity(new Intent(BrowserActivity.this, SettingActivity.class));
										break;
									case 5:
										BrowserUtil.copyURL(BrowserActivity.this, agentWeb.getUrl());
										break;
									case 6:
										if (agentWeb != null) {
											if (agentWeb.getUrl().contains(BrowserUtil.URL_SCHEME_HTTP)) {
												agentWeb.loadUrl("javascript:(function(){eruda.destroy()})()");
											} else {
												agentWeb.loadUrl("javascript:(function(){var script=document.createElement('script');script.src='http://eruda.liriliri.io/eruda.min.js';document.body.appendChild(script);script.onload=function(){eruda.init();eruda.show();};})()");
											}
										}
										break;
									default:
										break;
								}
							}
						});
					dialog.setContentView(dialogView);
					if (!dialog.isShowing()) dialog.show();
				}
			});
		
		topMenu.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					PopupMenu popup = new PopupMenu(BrowserActivity.this, v);
                    popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							switch (item.getItemId()) {
								case R.id.action_add_bookmark:
									if (agentWeb != null) {
										SQLiteHelper sql = new SQLiteHelper(BrowserActivity.this);
										sql.add_history(getApplicationContext(), agentWeb.getTitle(), agentWeb.getUrl(), 1); // 添加url到数据库
										Toast.makeText(getApplicationContext(), "已加入收藏!", Toast.LENGTH_SHORT).show();
									}
									break;
								case R.id.action_add_to_homescreen:
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
								case R.id.action_share:
									try {
										IntentUtil.share(BrowserActivity.this, agentWeb.getUrl());
									} catch (Exception e) {
									}
										break;
								case R.id.action_find:
									getWindow().setSoftInputMode(16);
									finderBar.setVisibility(0);

									new Handler().postDelayed(new Runnable() {
											public void run() {
												BrowserUtil.showSoftInput(BrowserActivity.this, finderBar.getEtKeyWords());
											}
										}, 60);
									break;
								case R.id.action_save_pic:
									if (agentWeb != null) {
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
									}
									break;
								case R.id.action_save_pdf:
									if (agentWeb != null) {
										if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
											PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
											PrintDocumentAdapter printAdapter = agentWeb.createPrintDocumentAdapter();
											String jobName = getString(R.string.app_name) + " Document";
											printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
										} else {
											Toast.makeText(getApplicationContext(), "当前系统不支持该功能", Toast.LENGTH_SHORT).show();
										}
									}
									break;
								case R.id.action_save_mth:
									if (agentWeb != null) {
										File file2 = new File(Environment.getExternalStorageDirectory()
															  + File.separator+agentWeb.getTitle()+".mht");
										agentWeb.saveWebArchive(file2.getAbsolutePath());
										ToastUtil.show(BrowserActivity.this, "已保存mth");
									}
									break;
								case R.id.action_save_html:
								
									break;
								case R.id.action_translation_page:
									if (agentWeb != null) {
										agentWeb.loadUrl(new StringBuffer().append(new StringBuffer().append("http://translate.baiducontent.com/transpage?query=").append(agentWeb.getUrl()).toString()).append("&from=auto&to=en&source=url").toString());
									}
									break;
								case R.id.action_source_page:
									if (agentWeb != null) {
										sourceEvent = "网页源码";
										getSource();
									}
									break;
								case R.id.action_resource_links:
									if (agentWeb != null) {
										sourceEvent = "资源链接";
										getSource();
									}
									break;
								case R.id.action_reading_mode:
									if (agentWeb != null) {
										sourceEvent = "阅读模式";
										getSource();
									}
									break;
								default:
									break;
							}
							return true;
						}
					});
					popup.show();
				}
			});
		
		topReload.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (agentWeb.getProgress() == 100) {
						agentWeb.reload();
					} else {
						agentWeb.stopLoading();
					}
				}
			});
			
		
		
		

		if (BrowserUtil.isURL(BrowserUtil.ClipboardUrl(this))) {
			Snackbar.make(coordinatorLayout, BrowserUtil.ClipboardUrl(this), Snackbar.LENGTH_LONG).setAction("打开", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						agentWeb.loadUrl(BrowserUtil.ClipboardUrl(BrowserActivity.this));
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
					BrowserUtil.hideSoftInput(BrowserActivity.this);
					agentWeb.clearMatches();
				}
			});
        agentWeb.setFindListener(new AgentWeb.FindListener() {
				public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
					finderBar.setMatchesNumber(activeMatchOrdinal + REQUEST_MENU_RESULT, numberOfMatches);
				}
			});
				
	}
	
	private void initWebView() {
		agentWeb.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					view.getSettings().setBlockNetworkImage(true);
					
					if (url.contains("baidu.com/wap/link") || url.contains("baidu.com/wap/shareview") || url.contains("baidu.com/s/") || url.contains("baidu.com/wap/view")) {
						agentWeb.getSettings().setUserAgentString(BrowserUtil.UA_SYMBIAN);
					} else {
						agentWeb.getSettings().setUserAgentString(agentWeb.getSettings().getUserAgentString());
					}
					
					if (url.contains("github.com")) {
						agentWeb.getSettings().setUserAgentString(BrowserUtil.UA_DESKTOP);
					} else {
						agentWeb.getSettings().setUserAgentString(agentWeb.getSettings().getUserAgentString());
					}
					
					if (url.contains("file")) {
						topUrl.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
					} else if (url.contains("https")) {
						topUrl.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_safe_website, 0, 0, 0);
					} else {
						topUrl.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_not_safe_website, 0, 0, 0);
					}

				}
				private void updateStatusColor(WebView view) {
					if(Build.VERSION.SDK_INT >= 21) {
						Bitmap var1 = BrowserUtil.getBitmapFromView(view);
						if(var1 != null) {
							int var2 = var1.getPixel(getWindowManager().getDefaultDisplay().getWidth() / 2, 10);
							int var3 = Color.red(var2);
							int var4 = Color.green(var2);
							int var5 = Color.blue(var2);
							if(Build.VERSION.SDK_INT >= 23) {
								if(var3 > 200 && var4 > 200 && var5 > 200) {
									getWindow().getDecorView().setSystemUiVisibility(8192);
									updateViewColor(0);
								} else {
									getWindow().getDecorView().setSystemUiVisibility(0);
									updateViewColor(1);
								}
							}
							topLayout.setBackgroundColor(var2);
							bottomLayout.setBackgroundColor(var2);
							BrowserUtil.setWindowStatusBarColor(BrowserActivity.this, var2);
							var1.recycle();
						}
					}
				}

				private void updateViewColor(int i) {
					if (i == 0 ) {
						topMenu.setColorFilter(Color.BLACK);
						topReload.setColorFilter(Color.BLACK);
						BrowserUtil.setCompoundTint(topUrl, Color.BLACK, true);
						
						bottomBack.setColorFilter(Color.BLACK);
						bottomForward.setColorFilter(Color.BLACK);
						bottomHome.setColorFilter(Color.BLACK);
						bottomTab.setColorFilter(Color.BLACK);
						bottomMenu.setColorFilter(Color.BLACK);
					} else if (i == 1) {
						topMenu.setColorFilter(Color.WHITE);
						topReload.setColorFilter(Color.WHITE);
						BrowserUtil.setCompoundTint(topUrl, Color.WHITE, true);
						
						bottomBack.setColorFilter(Color.WHITE);
						bottomForward.setColorFilter(Color.WHITE);
						bottomHome.setColorFilter(Color.WHITE);
						bottomTab.setColorFilter(Color.WHITE);
						bottomMenu.setColorFilter(Color.WHITE);
					}
				}
				 
				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					view.getSettings().setBlockNetworkImage(false);
					
					updateStatusColor(view);
					
					if (agentWeb.canGoForward()) {
						bottomForward.setEnabled(true);
						//bottomForward.setImageResource(R.drawable.ic_arrow_right);
					} else {
						bottomForward.setEnabled(false);
						//bottomForward.setImageResource(R.drawable.ic_arrow_right_disable);
					} if (agentWeb.canGoBack()) {
						bottomBack.setEnabled(true);
						//bottomBack.setImageResource(R.drawable.ic_arrow_left);
					} else {
						bottomBack.setEnabled(false);
						//bottomBack.setImageResource(R.drawable.ic_arrow_left_disable);
					} if (url.equals(BrowserUtil.HOME_PAGE)) {
						bottomHome.setEnabled(false);
						//bottomHome.setImageResource(R.drawable.ic_home_disabled);
					} else {
						bottomHome.setEnabled(true);
						//bottomHome.setImageResource(R.drawable.ic_home);
					}
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
				public boolean shouldOverrideUrlLoading(WebView var1, String var2) {
					if(!var2.startsWith("http:") && !var2.startsWith("https:") && !var2.startsWith("file:")) {
						final Intent var3 = new Intent("android.intent.action.VIEW", Uri.parse(var2));
						if(var3.resolveActivity(BrowserActivity.this.getPackageManager()) != null) {
							if(!var2.startsWith("tel:") && !var2.startsWith("sms:") && !var2.startsWith("mailto:")) {
								Snackbar.make(coordinatorLayout, var2, Snackbar.LENGTH_LONG).setAction("打开", new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											startActivity(var3);
										}
									}).show();
							} else {
								startActivity(var3);
							}
						}

						return true;
					} else {
						return super.shouldOverrideUrlLoading(var1, var2);
					}
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
					topUrl.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_dangerous_website, 0, 0, 0);
					
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
				public View getVideoLoadingProgressView() {
					LayoutInflater inflater = LayoutInflater.from(BrowserActivity.this);
					return inflater.inflate(R.layout.video_loading_progress, null);
				}
				@Override
				public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
					Message href = view.getHandler().obtainMessage();
					view.requestFocusNodeHref(href);
					String url = href.getData().getString("url");
					
					WebView newWebView = new WebView(view.getContext());  
					newWebView.setWebViewClient(new WebViewClient() {  
							@Override  
							public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
					agentWeb.destroy();
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
									topReload.setImageResource(R.drawable.ic_reload);
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
						topReload.setImageResource(R.drawable.ic_stop);
					}
				}
				@Override
				public void onReceivedTitle(WebView view, String title) {
					super.onReceivedTitle(view, title);
					topUrl.setText(view.getUrl());
					
				}
				@Override
				public void onReceivedIcon(WebView view, Bitmap icon) {
					super.onReceivedIcon(view, icon);
					//favTool.setImageBitmap(icon);
				}
				
				// 播放网络视频时全屏会被调用的方法
				public void onShowCustomView(View view, CustomViewCallback callback) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // 设置
					browserFrame.removeView(agentWeb);
					// 如果一个视图已经存在，那么立刻终止并新建一个
					if (customView != null) { // 如果不是空就把他变为空
						callback.onCustomViewHidden(); // 隐藏视图
						return;
					}
					browserFrame.addView(view); // 将全屏后获取到的视图添加到全屏控件去
					customView = view; // 自定义视图赋值
					customViewCallback = callback; // 保存自定义视图回调

					/* 隐藏控件 */
					topLayout.setVisibility(View.GONE);
					bottomLayout.setVisibility(View.GONE);
				}
				// 视频播放退出全屏会被调用的
				@Override
				public void onHideCustomView() {
					if (customView == null) // 不是全屏播放状态
						return;

					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					browserFrame.removeView(customView); // 删除存储的视图回调
					customView = null; // 置空
					browserFrame.addView(agentWeb);
					customViewCallback.onCustomViewHidden();

					/* 显示控件 */
					topLayout.setVisibility(View.VISIBLE);
					bottomLayout.setVisibility(View.VISIBLE);
				}
				@Override
				public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
					super.onGeolocationPermissionsShowPrompt(origin, callback);
					Snackbar.make(coordinatorLayout, "请求访问地理位置", Snackbar.LENGTH_INDEFINITE).setAction("允许", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								callback.invoke(origin, true, true);
							}
						}).setCallback(new Snackbar.Callback() {
							@Override
							public void onDismissed(Snackbar snackbar, int event) {
								if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_CONSECUTIVE) {
									callback.invoke(origin, false, true);
							 	}
							}
							@Override
							public void onShown(Snackbar snackbar) {
							}
							}).show();
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
							startActivityForResult(intent, IntentUtil.REQUEST_FILE_21);
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
					if (BrowserUtil.hasApp(BrowserActivity.this, "com.dv.adm.pay")) {
						BrowserUtil.downloadByADM(BrowserActivity.this, url, mimeType);
					} else {
						Snackbar.make(coordinatorLayout, contentDisposition, Snackbar.LENGTH_INDEFINITE).setAction("下载", new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									BrowserUtil.download(BrowserActivity.this, url, contentDisposition, mimeType);
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
										BrowserUtil.copyURL(BrowserActivity.this, target);
										break;
									case 2:
										String findimg = new StringBuffer().append("http://pic.sogou.com/pic/ris_searchList.jsp?statref=home&v=5&ul=1&keyword=").append(target).toString();
										agentWeb.loadUrl(findimg);
										break;
									case 3:
										if (BrowserUtil.hasApp(BrowserActivity.this, "com.dv.adm.pay")) {
											BrowserUtil.downloadByADM(BrowserActivity.this, target, BrowserUtil.MIME_TYPE_IMAGE);
										} else {
											Snackbar.make(coordinatorLayout, target, Snackbar.LENGTH_INDEFINITE).setAction("下载", new View.OnClickListener() {
													@Override
													public void onClick(View v) {
														BrowserUtil.download(BrowserActivity.this, target, target, BrowserUtil.MIME_TYPE_IMAGE);
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
		
	}
	
	protected class MyGridAdapter extends BaseAdapter {
		int[] icons = { R.drawable.ic_menu_bookmarks,R.drawable.ic_menu_history, R.drawable.ic_menu_offline_page, R.drawable.ic_menu_downloads, R.drawable.ic_menu_settings,
			R.drawable.ic_menu_copy_link, R.drawable.ic_menu_developer, R.drawable.ic_menu_computer_mode, R.drawable.ic_menu_image, R.drawable.ic_menu_full_screen };
		String [] titles = { "书签列表", "历史记录", "离线页面", "下载内容", "浏览设置",
			"复制链接", "开发者", "桌面模式", "无图模式", "全屏模式" };
        @Override
        public int getCount() {
            return titles.length;
        }
        @Override
        public Object getItem(int position) {
            return null;
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(BrowserActivity.this, R.layout.grid_item, null);
            TextView title =  (TextView) view.findViewById(R.id.text);
			ImageView imageView = (ImageView) view.findViewById(R.id.image);
            title.setText(titles[position]);
			imageView.setImageResource(icons[position]);//将图片更换

            return view;
        }
	}
	
}
