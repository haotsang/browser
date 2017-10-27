package small.indeed.fortunate.Activity;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.net.*;
import android.net.http.*;
import android.os.*;
import android.support.design.*;
import android.support.design.widget.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.text.method.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import android.widget.AdapterView.*;
import java.util.*;
import small.indeed.fortunate.Activity.*;
import small.indeed.fortunate.Unit.*;
import small.indeed.fortunate.View.*;

import android.support.v7.app.AlertDialog;

public class BrowserActivity extends AppCompatActivity 
{
	private CoordinatorLayout coordinatorLayout;
	private SwipeRefreshLayout swipeRefreshLayout;
	private AgentWeb agentWeb;
	private FloatingActionButton fab;
	
	private View customView;
    private VideoView videoView;
    private int originalOrientation;
    private WebChromeClient.CustomViewCallback customViewCallback;
	private ValueCallback<Uri[]> filePathCallback = null;
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		initUi();
		initWebView();
	}
	
	@Override
    protected void onDestroy() {
        if (agentWeb != null) {
            agentWeb.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            agentWeb.clearHistory();

            ((ViewGroup) agentWeb.getParent()).removeView(agentWeb);
			agentWeb.removeAllViews();
            agentWeb.destroy();
            agentWeb = null;
        }
        super.onDestroy();
    }
	
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return onKeyCodeBack();
		}
		return super.onKeyLongPress(keyCode, event);
	}
	@Override
	public void onBackPressed() {
		if (agentWeb.canGoBack()) {
			agentWeb.goBack();
		} else {
			onKeyCodeBack();
		}
	}
	
	private boolean onKeyCodeBack() {
		Vibrator vibrator = ( Vibrator ) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
		vibrator.vibrate(50);
		finish();
		return true;
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
	
	private void showOverflow() { 
		final BottomSheetDialog dialog = new BottomSheetDialog(this);
        ListView listView = new ListView(this);
		listView.setDividerHeight(0);

		ArrayList<String> list = new ArrayList<String>();
		list.add(0, "新建标签页");
		list.add(1, "返回上一页");
		list.add(2, "分享链接");
		list.add(3, "添加到桌面");
		if (agentWeb.getSettings().getUserAgentString().equals(BrowserUnit.UA_DESKTOP)) {
			list.add(4, "访问手机版页面");
		} else {
			list.add(4, "访问电脑版页面");
		}
		list.add(5, "设置");
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					dialog.dismiss();
					switch (position) {
						case 0:
							Intent intent = new Intent(BrowserActivity.this, BrowserActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
							startActivity(intent);
							break;
						case 1:
							if (agentWeb.canGoForward()) {
								agentWeb.goForward();
							}
							break;
						case 2:
							try {
								IntentUnit.share(BrowserActivity.this, agentWeb.getUrl());
							} catch (Exception e) {
							}
							break;
						case 3:
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
						case 4:
							if (agentWeb.getSettings().getUserAgentString().equals(BrowserUnit.UA_DESKTOP)) {
								agentWeb.getSettings().setUserAgentString(null);
								agentWeb.reload();
							} else {
								agentWeb.getSettings().setUserAgentString(BrowserUnit.UA_DESKTOP);
								agentWeb.reload();
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
        dialog.setContentView(listView);
        dialog.show();
	}
	
	private void initUi()
	{
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
		agentWeb = (AgentWeb) findViewById(R.id.page);
		coordinatorLayout=(CoordinatorLayout) findViewById(R.id.container);
		fab=(FloatingActionButton)findViewById(R.id.fab);
		
		Uri intentData = getIntent().getData();
		String intentAction = getIntent().getAction();
		String intentExtra = getIntent().getStringExtra("query");

		if (Intent.ACTION_VIEW.equals(intentAction)) {
			if (intentData != null) {
				this.agentWeb.loadUrl(intentData.toString());
			}
		} else if (Intent.ACTION_WEB_SEARCH.equals(intentAction)) {
			if (intentExtra.length() > 0) {
				this.agentWeb.loadUrl(BrowserUnit.SEARCH_ENGINE_BAIDU + intentExtra);
			} else if (intentExtra.startsWith(BrowserUnit.URL_SCHEME_HTTP) || intentExtra.startsWith(BrowserUnit.URL_SCHEME_HTTPS) || intentExtra.startsWith(BrowserUnit.URL_SCHEME_FILE)) {
				this.agentWeb.loadUrl(intentExtra);
			}
		} else {
			this.agentWeb.loadUrl(BrowserUnit.BASE_URL);
		}
		
		if (ClipboardUrl().startsWith("http://") || ClipboardUrl().startsWith("https://")) {
			Snackbar.make(coordinatorLayout, "剪贴板发现网址。是否打开？", Snackbar.LENGTH_LONG).setAction(android.R.string.ok, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						agentWeb.loadUrl(ClipboardUrl());
					}
				}).show();
		}
		//首次启动刷新页面
        swipeRefreshLayout.post(new Runnable() {
				@Override
				public void run() {
					swipeRefreshLayout.setRefreshing(true);
					agentWeb.reload();
				}
			});
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
												   
		fab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showOverflow();
				}
			});
	}

	private void initWebView()
	{
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
				public void onProgressChanged(WebView view, int progress) {
					super.onProgressChanged(view, progress);
					if(progress == 100){
						swipeRefreshLayout.setRefreshing(false);
					}else if(!swipeRefreshLayout.isRefreshing()){
						swipeRefreshLayout.setRefreshing(true);
					}
				}
				@Override
				public void onReceivedTitle(WebView view, String title) {
					super.onReceivedTitle(view, title);
					//getActionBar().setTitle(title.toString().length() == 0 ? view.getUrl() : title);
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
					Snackbar.make(coordinatorLayout, "允许网页访问地理位置", Snackbar.LENGTH_LONG).setAction(android.R.string.ok, new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								callback.invoke(origin, true, true);
							}
						}).show();
//						.setAction(android.R.string.cancel, new View.OnClickListener() {
//							@Override
//							public void onClick(View v) {
//								callback.invoke(origin, false, true);
//							}
//						})
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
						Snackbar.make(coordinatorLayout, "确定下载？", Snackbar.LENGTH_LONG).setAction(android.R.string.ok, new View.OnClickListener() {
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
						list.add(2, getString(R.string.dialog_longpress_save_img));
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
										BrowserUnit.copyURL(BrowserActivity.this, target);
										break;
									case 2:
										if (BrowserUnit.hasApp(BrowserActivity.this, "com.dv.adm.pay")) {
											BrowserUnit.downloadByADM(BrowserActivity.this, target, BrowserUnit.MIME_TYPE_IMAGE);
										} else {
											Snackbar.make(coordinatorLayout, "确定下载？", Snackbar.LENGTH_LONG).setAction(android.R.string.ok, new View.OnClickListener() {
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
	}
	
}
