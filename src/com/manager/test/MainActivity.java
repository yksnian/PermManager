package com.manager.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {  
    private ListView lv;  
    private MyAdapter adapter;  

    ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();  
     
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.main);  
          
        lv = (ListView)findViewById(R.id.lv);  
        PackageManager pm = getPackageManager();  
        List<PackageInfo> installed = pm.getInstalledPackages(0);  
          
        for(PackageInfo pi:installed){  
            HashMap<String, Object> map = new HashMap<String, Object>();  
            //显示用户安装的应用程序，而不显示系统程序  
          if((pi.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==0&&  
                  (pi.applicationInfo.flags&ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)==0){  
              map.put("icon", pi.applicationInfo.loadIcon(pm));//图标  
              map.put("appName", pi.applicationInfo.loadLabel(pm));//应用程序名称  
              map.put("packageName", pi.applicationInfo.packageName);//应用程序包名  
              items.add(map);  
          }  
        }  
        
        adapter = new MyAdapter(this, items, R.layout.app_list_item,   
                new String[]{"icon", "appName", "packageName"},  
                new int[]{R.id.icon, R.id.appName, R.id.packageName});  
        
        lv.setAdapter(adapter);  
        
    }   
}  
  
class MyAdapter extends SimpleAdapter  
{  
    private int[] appTo;  
    private String[] appFrom;    
    private List<? extends Map<String, ?>>  appData;  
    private int appResource;  
    private LayoutInflater appInflater;  
    private int mPosition;
    private Context mContext;
    
    public MyAdapter(Context context, List<? extends Map<String, ?>> data,  
            								int resource, String[] from, int[] to) {  
        super(context, data, resource, from, to);  
        mContext = context;
        appData = data;  
        appResource = resource;  
        appFrom = from;  
        appTo = to;  
        appInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
    }  
      
    public View getView(int position, View convertView, ViewGroup parent){ 
    	mPosition = position;
        return createViewFromResource(position, convertView, parent, appResource);  
    }  
      
    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource){  
        View v;  
        if(convertView == null){  
            v = appInflater.inflate(resource, parent,false);  
            final int[] to = appTo;  
            final int count = to.length;  
            final View[] holder = new View[count];  
              
            for(int i = 0; i < count; i++){  
                holder[i] = v.findViewById(to[i]);  
            }  
            v.setTag(holder);  
        }else {  
            v = convertView;  
        }  
        
        bindView(position, v);  
        return v;     
    }  
      
    private void bindView(int position, View view){  
        final Map<String, ?> dataSet = appData.get(position);  
        
        if(dataSet == null){  
            return;  
        }  
           
        final View[] holder = (View[])view.getTag();  
        final String[] from = appFrom;  
        final int[] to = appTo;  
        final int count = to.length;  
          
        for(int i = 0; i < count; i++){  
            final View v = holder[i];  
            
            if(v != null){  
                final Object data = dataSet.get(from[i]);  
                String text = data == null ? "":data.toString();  
                
                if(text == null){  
                    text = "";  
                }  
                  
                if(v instanceof TextView){  
                    setViewText((TextView)v, text); 
                    v.setOnClickListener(new OnClickListener() {
                		
                		@Override
                		public void onClick(View v) {
                			// TODO Auto-generated method stub
                			Intent intent = new Intent(Intent.ACTION_MAIN);
                			intent.setPackage("com.manager.test");
                			intent.putExtra("appName", (String) dataSet.get("appName"));
                			intent.putExtra("packName", (String) dataSet.get("packageName"));
                			Log.d("ven","start activity [position] -- "+ getPosition()
                					+" app name -->"+dataSet.get("appName")
                     				+" package name -->"+ (String) dataSet.get("packageName"));
                			intent.setComponent(new ComponentName(mContext, PermCheckActivity.class));
                			mContext.startActivity(intent);
                		}
                	});
                }else if(v instanceof ImageView){  
                    setViewImage((ImageView)v, (Drawable)data);  
                }else {
                    throw new IllegalStateException(v.getClass().getName() + " is not a " +  
                            "view that can be bounds by this SimpleAdapter");  
                }
            }  
        }  
    }  
    
    public void setViewImage(ImageView v, Drawable value)  
    {  
        v.setImageDrawable(value);  
    }  
    
    private int getPosition() {
    	return mPosition;
    }
}  