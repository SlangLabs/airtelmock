package in.slanglabs.airtelmock;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import in.slanglabs.platform.application.SlangApplication;

public class RoamingPacksActivity extends AppCompatActivity {
    Integer[] imageIds = {
            R.drawable.onedaypack,
            R.drawable.tendaypack,
            R.drawable.thirtydaypack,
            R.drawable.monthly,
    };

    String[] entityNames = {
            "one_day_pack",
            "ten_day_pack",
            "thirty_day_pack",
            "monthly_pack",
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packs);

        ListView lv = (ListView) findViewById(R.id.packs);
        lv.setAdapter(new PacksAdapter(this, R.layout.listitem_packs, imageIds));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SlangApplication.getScreenContext().notifyEntityResolved("pack", entityNames[i]);
            }
        });
    }

    private class PacksAdapter extends ArrayAdapter<Integer> {
        private Integer[] mImageIds;

        public PacksAdapter(Context context, int id, Integer[] imageIds) {
            super(context, id, imageIds);
            mImageIds = imageIds;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater li = LayoutInflater.from(getContext());
                v = li.inflate(R.layout.listitem_packs, null);
            }
            ImageView image = v.findViewById(R.id.packs_item);
            image.setImageResource(imageIds[position]);
            return v;
        }
    }
}
