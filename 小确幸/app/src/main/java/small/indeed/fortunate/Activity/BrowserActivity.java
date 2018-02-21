package small.indeed.fortunate.Activity;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.net.*;
import android.net.http.*;
import android.os.*;
import android.preference.*;
import android.support.design.widget.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.text.*;
import android.text.method.*;
import android.view.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.AdapterView.*;
import android.widget.TextView.*;
import java.util.*;
import small.indeed.fortunate.*;
import small.indeed.fortunate.Unit.*;
import small.indeed.fortunate.View.*;

import android.support.v7.app.AlertDialog;
import android.text.ClipboardManager;

public class BrowserActivity extends AppCompatActivity {
	
	private CoordinatorLayout coordinatorLayout;
	private SwipeRefreshLayout swipeRefreshLayout;
	private AgentWeb agentWeb;
	private ProgressBar progressBar;
	
	private AppCompatAutoCompleteTextView inputBox;
	private AppCompatImageButton omniboxMulti;
	private AppCompatImageButton omniboxOverflow;
	
	private View customView;
    private VideoView videoView;
    private int originalOrientation;
    private WebChromeClient.CustomViewCallback customViewCallback;
	private ValueCallback<Uri[]> filePathCallback = null;
	
	
	private int[] icons = { R.drawable.ap, R.drawable.aw, R.drawable.a3, R.drawable.ay, R.drawable.a6, R.drawable.ax };
	private String [] titles = { "刷新网页", "发至桌面", "源码", "分享", "下载", "设置" };
	
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
		initOmnibox();
		initWebView();
	}
	
	@Override
    protected void onDestroy() {
        if (agentWeb != null) {
            agentWeb.clearHistory();
			agentWeb.clearCache(true);
			agentWeb.loadUrl("about:blank");

            ((ViewGroup) agentWeb.getParent()).removeView(agentWeb);
			agentWeb.removeAllViews();
			agentWeb.freeMemory();
			agentWeb.pauseTimers();
			agentWeb.destroy();
            agentWeb = null;
        }
        super.onDestroy();
    }
	
	@Override
	public void onBackPressed() {
		if (agentWeb.canGoBack()) {
			agentWeb.goBack();
		} else if (inputBox.hasFocus()) {
			BrowserUnit.hideSoftInput(BrowserActivity.this, inputBox);
		} else {
			super.onBackPressed();
		}
	}
	
	private void initOmnibox() {
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
		agentWeb = (AgentWeb) findViewById(R.id.page);
		progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
		
		inputBox = (AppCompatAutoCompleteTextView) findViewById(R.id.main_omnibox_inputbox);
		omniboxMulti = (AppCompatImageButton) findViewById(R.id.main_omnibox_multi);
		omniboxOverflow = (AppCompatImageButton) findViewById(R.id.main_omnibox_overflow);
		
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
			this.agentWeb.loadUrl("about:blank");
		}
		
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					agentWeb.reload();
				}
			});
        swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_light,
										  android.R.color.holo_red_light,
										  android.R.color.holo_orange_light,
										  android.R.color.holo_green_light);
		
		if (BrowserUnit.isURL(ClipboardUrl())) {
			Snackbar.make(coordinatorLayout, ClipboardUrl(), Snackbar.LENGTH_SHORT).setAction("打开", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						agentWeb.loadUrl(ClipboardUrl());
					}
				}).show();
		}
		
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(BrowserActivity.this, android.R.layout.simple_list_item_1, titles);
		inputBox.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		inputBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String query = inputBox.getText().toString().trim();

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
				}
			});

		inputBox.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					String query = inputBox.getText().toString().trim();
					if (query.isEmpty()) {
						return true;
					} else {
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
					}
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
						
					} else {
						
					}
				}}
		);

		inputBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override  
				public void onFocusChange(View v, boolean hasFocus) {  
					if (hasFocus) {//获得焦点
						inputBox.showDropDown();
					} else {//失去焦点
						BrowserUnit.hideSoftInput(BrowserActivity.this, inputBox);
					}  
				}             
			});
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		omniboxMulti.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final SheetDialog dialog = new SheetDialog(BrowserActivity.this);
					View contentView = View.inflate(BrowserActivity.this, R.layout.activity_main, null);
					dialog.setContentView(contentView);
					dialog.show();
				}
			});
			
		omniboxOverflow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (agentWeb.getProgress() == 100) {
						showOverflow();
					} else {
						agentWeb.stopLoading();
					}
				}
		});
		
	}
	
	private void showOverflow() {
		final SheetDialog dialog = new SheetDialog(BrowserActivity.this);
		View contentView = View.inflate(this, R.layout.grid_view, null);
		GridView gridView = (GridView) contentView.findViewById(R.id.gridview);
		MyGridAdapter adapter = new MyGridAdapter();
		gridView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		gridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
										int position, long id) {
					dialog.dismiss();
					switch (position) {
						case 0:
							agentWeb.reload();
							break;
						case 1:
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
								Snackbar.make(coordinatorLayout, R.string.toast_successful, Snackbar.LENGTH_SHORT).show();
							} catch (Exception e) {
								Snackbar.make(coordinatorLayout, R.string.toast_failed, Snackbar.LENGTH_SHORT).show();
							}
							break;
						case 2:
							agentWeb.loadUrl("javascript:alert(document.getElementsByTagName('html')[0].innerHTML);");
							break;
						case 3:
							try {
								IntentUnit.share(BrowserActivity.this, agentWeb.getUrl());
							} catch (Exception e) {}
							break;
						case 4:
							//通过包名启动应用
							try { 
								Intent i = getPackageManager().getLaunchIntentForPackage("com.dv.adm.pay");
								startActivity(i);
							} catch (Exception e) { 
								Intent i = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
								startActivity(i); 
							}
							break;
						case 5:
							startActivity(new Intent(BrowserActivity.this, SettingActivity.class));
							break;
						default:
							break;
					}
				}
			});
			dialog.setContentView(contentView);
			dialog.show();
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
				public void onProgressChanged(WebView view, int newProgress) {
					super.onProgressChanged(view, newProgress);
					if(newProgress == 100) {
						swipeRefreshLayout.setRefreshing(false);
					} else if (!swipeRefreshLayout.isRefreshing()) {
						swipeRefreshLayout.setRefreshing(true);
					}
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
									omniboxOverflow.setImageResource(R.drawable.ag);
									progressBar.setVisibility(View.GONE);
									progressBar.setAlpha(1);
								}
								@Override
								public void onAnimationCancel(Animator animation) {}
								@Override
								public void onAnimationRepeat(Animator animation) {}
							});
						outAni.start();
					} else {
						//进度条加载时始终使其可见
						omniboxOverflow.setImageResource(R.drawable.a2);
						progressBar.setVisibility(View.VISIBLE);
					}
				}
				@Override
				public void onReceivedTitle(WebView view, String title) {
					super.onReceivedTitle(view, title);
					inputBox.setText(title.toString().length() == 0 ? view.getUrl() : title);
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
						Snackbar.make(coordinatorLayout, contentDisposition, Snackbar.LENGTH_INDEFINITE).setAction("下载", new View.OnClickListener() {
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
					list.add(1, getString(R.string.dialog_longpress_new_tab_in_background));
					list.add(2, getString(R.string.dialog_longpress_copy_link));
					if (result != null && (result.getType() == WebView.HitTestResult.IMAGE_TYPE || result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
						list.add(3, getString(R.string.dialog_longpress_save_img));
					}
					
					AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
					builder.setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
									case 0:
										Intent intent = new Intent(BrowserActivity.this, BrowserActivity.class);
										intent.setData(Uri.parse(target));
										intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
										startActivity(intent, ActivityOptions.makeBasic().toBundle());
										break;
									case 1:
										Intent intent2 = new Intent(BrowserActivity.this, BrowserActivity.class);
										intent2.setData(Uri.parse(target));
										intent2.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
										startActivity(intent2, ActivityOptions.makeTaskLaunchBehind().toBundle());
										break;
									case 2:
										BrowserUnit.copyURL(BrowserActivity.this, target);
										break;
									case 3:
										if (BrowserUnit.hasApp(BrowserActivity.this, "com.dv.adm.pay")) {
											BrowserUnit.downloadByADM(BrowserActivity.this, target, BrowserUnit.MIME_TYPE_IMAGE);
										} else {
											Snackbar.make(coordinatorLayout, target, Snackbar.LENGTH_INDEFINITE).setAction("下载", new View.OnClickListener() {
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
						if ((y - location) > 10) {
							if (agentWeb.getScrollY() < 5) {
							//	fab.hide();
							} else {
								//fab.show();
							}
						} else if ((y - location) < -10) {
							//fab.hide();
						}
						location = 0;
					}

					return false;
				}

			});
			
	}
	
	protected class MyGridAdapter extends BaseAdapter {
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
