package com.manager.test;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.manager.test.MyListAdapter.ViewHolder;

public class PermCheckActivity extends Activity {  
    private ListView lv;  
    private MyListAdapter mAdapter;  
    private ArrayList<String> list;  
    private Button bt_save;  
    private Button bt_cancel;  
    private Button bt_selectall;  
    private TextView mTv;
    private TextView mTvBefore;
    private TextView mTvAfter;
    private String mPkgName;
    private String mAppName;
    private ContentResolver mResolver;
    /** Called when the activity is first created. */  
  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        this.requestWindowFeature(Window. FEATURE_NO_TITLE ); 
        Log.d("ven","-------------- on create -----------------");
        mPkgName = (String) getIntent().getCharSequenceExtra("packName");
        mAppName = (String) getIntent().getCharSequenceExtra("appName");
        setContentView(R.layout.permlistcheck);   
        lv = (ListView) findViewById(R.id.lv);  
        bt_selectall = (Button) findViewById(R.id.bt_selectall);  
        bt_cancel = (Button) findViewById(R.id.bt_cancel);  
        bt_save = (Button)findViewById(R.id.bt_save);  
        mTv = (TextView)findViewById(R.id.tv_notice);
        mTvBefore = (TextView)findViewById(R.id.tv_before);
        mTvAfter = (TextView)findViewById(R.id.tv_after);
        mTvBefore.setText(R.string.before);
        mTv.setText(mAppName);
        mTvAfter.setText(R.string.after);
        list = new ArrayList<String>(); 
        mResolver = this.getContentResolver();
        initData();   
        mAdapter = new MyListAdapter(list, this);  
        lv.setAdapter(mAdapter);  
        bt_selectall.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                for (int i = 0; i < list.size(); i++) {  
                    MyListAdapter.getIsSelected().put(i, true);  
                }   
                dataChanged();  
            }  
        });  
        
        bt_save.setOnClickListener(new OnClickListener() {
			//ven，保存之前，遍历数据库,将package name为需要插入package 的权限从数据库里删除
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				try {
					if( mResolver != null)
						mResolver.delete(PermProvider.CONTENT_URI, "package=?", new String[]{mPkgName});
				}catch(Exception ex) {
					Log.d("ven"," ----delete exception ----");
				}		    	
				for (int i = 0; i < list.size(); i++) {
					if(list.get(i)!=null && MyListAdapter.getIsSelected().get(i)) {
						ContentValues cv = new ContentValues();
						cv.put("package", mPkgName);
						cv.put("permission", list.get(i));
						try {
							if( mResolver != null) {
								mResolver.insert(PermProvider.CONTENT_URI, cv);
							}
						}catch(Exception e) {
							Log.e("ven", "[PermCheckActivity] insert exception");
						}
					}		
				}
				Toast.makeText(PermCheckActivity.this, "保存完毕！", Toast.LENGTH_SHORT).show();
				onBackPressed();
			}
		});
        
        bt_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				for (int i = 0; i < list.size(); i++) {  
                  if (MyListAdapter.getIsSelected().get(i)) {  
                      MyListAdapter.getIsSelected().put(i, false);  
                  }  
				}  
              	dataChanged();
			}
		});
  

        lv.setOnItemClickListener(new OnItemClickListener() {  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
                    long arg3) {  
                ViewHolder holder = (ViewHolder) arg1.getTag();  
                holder.cb.toggle();   
                MyListAdapter.getIsSelected().put(arg2, holder.cb.isChecked());  
                dataChanged();
            }  
        });  
    }  
  
    private void initData() {  
    	PackageManager pm = getPackageManager(); 
        PackageInfo pkgInfo = null ;
        String[] perms = null;

        try {
        	 pkgInfo = pm.getPackageInfo(mPkgName, PackageManager.GET_PERMISSIONS);
        } catch (Exception e) {
        	Log.d("ven","get pkginfo error");
        }
        
        if(pkgInfo != null) {
        	perms = pkgInfo.requestedPermissions;
        	if(perms != null) {
		        for(String info : perms)  {
		        	Log.e("ven", " permission ===>  " + info);
		        	list.add(info);
		        }
        	}
        }
    }  

    private void dataChanged() {  
        // 通知listView刷新  
        mAdapter.notifyDataSetChanged();  
    }  
    
    String getCheckPkgName() {
    	return mPkgName;
    }
}

class MyListAdapter extends BaseAdapter {    
    private ArrayList<String> mList;  
    private static HashMap<Integer, Boolean> mSelected;  
    private Context mContext;  
    private LayoutInflater inflater = null;  

    public MyListAdapter(ArrayList<String> list, Context context) {  
        mContext = context;  
        mList = list;  
        inflater = LayoutInflater.from(context);  
        mSelected = new HashMap<Integer, Boolean>();  
        for (int i = 0; i < mList.size(); i++) {  
            if(permissionDenied(i)) {
            	getIsSelected().put(i, true);
            } else {
            	getIsSelected().put(i, false);
            }
        }    
    }  
   
    @Override  
    public int getCount() {  
        return mList.size();  
    }  
  
    @Override  
    public Object getItem(int position) {  
        return mList.get(position);  
    }  
  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
        ViewHolder holder = null;  
        if (convertView == null) {  
            holder = new ViewHolder();   
            convertView = inflater.inflate(R.layout.perm_chk_item, null);  
            holder.tv = (TextView) convertView.findViewById(R.id.item_tv);  
            holder.cb = (CheckBox) convertView.findViewById(R.id.item_cb);  
            convertView.setTag(holder);  
        } else {  
            holder = (ViewHolder) convertView.getTag();  
        }  
        holder.tv.setText(mList.get(position));   
        holder.cb.setChecked(getIsSelected().get(position));
        return convertView;  
    }  
  
    public static HashMap<Integer, Boolean> getIsSelected() {  
        return mSelected;  
    }  
  
    public static class ViewHolder {  
        TextView tv;  
        CheckBox cb;  
    }
    
    public boolean permissionDenied(int position) {
    	String pkg = ((PermCheckActivity)mContext).getCheckPkgName();
    	Log.d("ven", "[PermCheckActivity]---------pkg --- name ---------"+pkg);
    	String permName = mList.get(position);
    	Cursor c = mContext.getContentResolver().query(PermProvider.CONTENT_URI, null,
    			"permission=?"+" AND "+"package=?", new String[]{permName, pkg} , "_id desc");
    	if(c !=null && c.getCount() >0) {
    		return true;
    	}
    	return false;
    }
}  