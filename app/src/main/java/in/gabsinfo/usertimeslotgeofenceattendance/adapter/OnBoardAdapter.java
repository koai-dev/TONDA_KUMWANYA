package in.gabsinfo.usertimeslotgeofenceattendance.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.annotations.NotNull;

import in.gabsinfo.usertimeslotgeofenceattendance.R;

public class OnBoardAdapter extends PagerAdapter {

    private final String[] image_slide;
    private final int[] heading_slide;
    private final int[] description_slide;
    private Context context;

    public OnBoardAdapter(Context context, String[] image_slide, int[] heading_slide, int[] description_slide) {
        this.context = context;
        this.image_slide=image_slide;
        this.heading_slide=heading_slide;
        this.description_slide=description_slide;
    }


    @Override
    public int getCount() {
        return heading_slide.length;
    }

    @Override
    public boolean isViewFromObject(@NotNull View view, @NotNull Object object) {
        return view == object;
    }

    @NotNull
    @Override
    public Object instantiateItem(@NotNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.onboard_item, container,false);
        container.addView(view);

        LottieAnimationView slide_imageView = view.findViewById(R.id.imageView1);
        TextView slideHeading = view.findViewById(R.id.tvHeading);
        TextView  slideDescription = view.findViewById(R.id.tvDescription);

        slide_imageView.setAnimation(image_slide[position]);
        slideHeading.setText(heading_slide[position]);
        slideDescription.setText(description_slide[position]);

        return view;
    }



    @Override
    public void destroyItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
        container.removeView((RelativeLayout)object);
    }

//    @Override
//    public void destroyItem(ViewGroup container, int position, Object object) {
//        View view = (View) object;
//        container.removeView(view);
//    }

}
