package com.dodo.myrichtext;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dodo.myrichtext.bean.RichColorBean;
import com.dodo.myrichtext.richeditor.RichEditor;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class RichTextActivity extends AppCompatActivity implements View.OnClickListener {

    private InputMethodManager imm;//软键盘管理器
    private RichEditor mEditor;
    private ImageButton ib_Bold, ib_Italic, ib_Img;
    private Button bt_color;
    boolean isItalic = false;//是否下划线
    boolean isBold = false;//是否加粗
    boolean isColor = false;//是否变色

    private int mSizeSelect = 3;//选择的字号

    private int[] colorNums;
    private int mColorSelect = -1;//选择颜色
    private CommonAdapter mColorAdapter;//颜色适配器
    private GridView mGvColor;//颜色gridview
    private List<RichColorBean> mColorList = new ArrayList<>();//颜色集合


    public final static int RICH_IMAGE_CODE = 0x33;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_richtext);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        initViews();
        initEvents();
    }

    private void initViews() {
        //富文本编辑初始化
        mEditor = (RichEditor) findViewById(R.id.editor);
        mEditor.setEditorFontSize(14);
        mEditor.setPadding(10, 10, 10, 50);
        //mEditor.setPlaceholder("*项目详情：不得少于100字，说明项目的情况\\n如：项目介绍，筹款如何使用，自我介绍等");

        //字体布局
        ib_Bold = (ImageButton) findViewById(R.id.action_bold);
        ib_Italic = (ImageButton) findViewById(R.id.action_italic);
        ib_Img = (ImageButton) findViewById(R.id.action_image);
        bt_color = (Button) findViewById(R.id.action_color);

    }

    private void initEvents() {

        ib_Bold.setOnClickListener(this);
        ib_Italic.setOnClickListener(this);
        ib_Img.setOnClickListener(this);
    }


    /**
     * 设置字号大小
     */
    public void setFontSize(View v) {
        //mEditor.setFontSize(2);
        showRichSizeDialog();
    }

    /**
     * 设置颜色
     */
    public void setFontColor(View v) {
        showRichColorDialog();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //粗体
            case R.id.action_bold:
                ib_Bold.setImageResource(isBold ? R.mipmap.icon_rich_bold : R.mipmap.icon_rich_bold_ed);
                isBold = !isBold;
                mEditor.setBold();
                break;
            //斜体
            case R.id.action_italic:
                ib_Italic.setImageResource(isItalic ? R.mipmap.icon_rich_underline : R.mipmap.icon_rich_underline_ed);
                isItalic = !isItalic;
                mEditor.setUnderline();
                break;
            //图片
            case R.id.action_image:
                Intent picture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(picture, RICH_IMAGE_CODE);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RICH_IMAGE_CODE && resultCode == Activity.RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            //更新数据库
            Cursor c = this.getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String picturePath = c.getString(columnIndex);
            Log.i("dgs", "picturePath----" + picturePath);
            //插入图片
            mEditor.insertImage(picturePath, "图片");
            c.close();
            //获取图片并显示
        }

    }


    /**
     * 颜色弹出框弹出框
     */
    private void showRichColorDialog() {
        final Dialog dialog = new Dialog(this, R.style.noticeDialog);
        dialog.setContentView(R.layout.alert_rich_color_dialog);
        mGvColor = (GridView) dialog.findViewById(R.id.gv_color);
        getColorList();


        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);//显示位置
        // 设置宽高
        dialogWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialogWindow.setWindowAnimations(R.style.AnimBottom); //设置窗口弹出动画
        dialog.show();

        mGvColor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mEditor.setTextColor(mColorList.get(position).getColor());
                mColorSelect = position;
                //mColorList.get(position).setIsmake(true);
                bt_color.setTextColor(mColorList.get(position).getColor());
                dialog.cancel();
                
            }
        });


        // 设置监听
        TextView ok = (TextView) dialog.findViewById(R.id.set);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

    }

    /**
     * 颜色选择器布局
     */
    private void getColorList() {
        setColorList();
        mColorAdapter = new CommonAdapter<RichColorBean>(this, R.layout.item_rich_color, mColorList) {
            @Override
            protected void convert(ViewHolder viewHolder, RichColorBean item, int position) {
                TextView tv_color = viewHolder.getView(R.id.tv_rich_color);
                TextView tv_colored = viewHolder.getView(R.id.tv_rich_color_ed);
                if (mColorSelect == position) {//是否选中
                    tv_colored.setVisibility(View.VISIBLE);
                } else {
                    tv_colored.setVisibility(View.GONE);
                }

                GradientDrawable myGrad = (GradientDrawable) tv_color.getBackground();
                myGrad.setColor(item.getColor());
            }
        };
        mGvColor.setAdapter(mColorAdapter);
        mColorAdapter.notifyDataSetChanged();

    }

    /**
     * 把颜色值放到list
     */
    private void setColorList() {
        setColorNums();
        //mColorList.clear();
        if (mColorList.size() < 1) {
            for (int i = 0; i < colorNums.length; i++) {
                RichColorBean color = new RichColorBean();
                color.setColor(RichTextActivity.this.getResources().getColor(colorNums[i]));
                mColorList.add(color);
            }
        }

    }

    /**
     * 把颜色值放进数组
     */
    private void setColorNums() {

        colorNums = new int[]{R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5, R.color.color6,
                R.color.color7, R.color.color8, R.color.color9, R.color.color10, R.color.color11, R.color.color12,
                R.color.color13, R.color.color14, R.color.color15, R.color.color16, R.color.color17, R.color.color18,};

    }


    /**
     * 字号大小选择弹出框
     */
    private void showRichSizeDialog() {
        final Dialog dialog = new Dialog(this, R.style.noticeDialog);
        dialog.setContentView(R.layout.alert_rich_size_dialog);

        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);//显示位置
        // 设置宽高
        dialogWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialogWindow.setWindowAnimations(R.style.AnimBottom); //设置窗口弹出动画
        dialog.show();

        RelativeLayout rl_size1 = (RelativeLayout) dialog.findViewById(R.id.ll_rich_size1);
        RelativeLayout rl_size2 = (RelativeLayout) dialog.findViewById(R.id.ll_rich_size2);
        RelativeLayout rl_size3 = (RelativeLayout) dialog.findViewById(R.id.ll_rich_size3);
        RelativeLayout rl_size4 = (RelativeLayout) dialog.findViewById(R.id.ll_rich_size4);

        final ImageView img_1 = (ImageView) dialog.findViewById(R.id.iv_rich_size1_ed);
        final ImageView img_2 = (ImageView) dialog.findViewById(R.id.iv_rich_size2_ed);
        final ImageView img_3 = (ImageView) dialog.findViewById(R.id.iv_rich_size3_ed);
        final ImageView img_4 = (ImageView) dialog.findViewById(R.id.iv_rich_size4_ed);

        if (mSizeSelect == 1) {
            img_1.setVisibility(View.VISIBLE);
            img_2.setVisibility(View.GONE);
            img_3.setVisibility(View.GONE);
            img_4.setVisibility(View.GONE);
        } else if (mSizeSelect == 2) {
            img_2.setVisibility(View.VISIBLE);
            img_1.setVisibility(View.GONE);
            img_3.setVisibility(View.GONE);
            img_4.setVisibility(View.GONE);
        } else if (mSizeSelect == 3) {
            img_3.setVisibility(View.VISIBLE);
            img_2.setVisibility(View.GONE);
            img_1.setVisibility(View.GONE);
            img_4.setVisibility(View.GONE);
        } else if (mSizeSelect == 4) {
            img_4.setVisibility(View.VISIBLE);
            img_2.setVisibility(View.GONE);
            img_3.setVisibility(View.GONE);
            img_1.setVisibility(View.GONE);
        }


        rl_size1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setFontSize(4);
                mSizeSelect = 1;
                dialog.cancel();
            }
        });
        rl_size2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setFontSize(3);
                mSizeSelect = 2;
                dialog.cancel();

            }
        });
        rl_size3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setFontSize(2);
                mSizeSelect = 3;
                dialog.cancel();

            }
        });
        rl_size4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setFontSize(1);
                mSizeSelect = 4;
                dialog.cancel();

            }
        });


        // 设置监听
        TextView ok = (TextView) dialog.findViewById(R.id.set);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

    }


}

