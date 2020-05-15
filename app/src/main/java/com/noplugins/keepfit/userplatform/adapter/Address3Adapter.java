package com.noplugins.keepfit.userplatform.adapter;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.noplugins.keepfit.userplatform.R;
import com.noplugins.keepfit.userplatform.bean.AddressBean;

import java.util.List;

public class Address3Adapter extends BaseQuickAdapter<AddressBean.Area, BaseViewHolder> {
    private Context context; //tv_address
    private List<AddressBean.Province> mChangList;
    private List<AddressBean.Province> mPrivateList;
    private List<AddressBean.Province> mTeamList;

    public Address3Adapter(@Nullable List<AddressBean.Area> data) {
        super(R.layout.item_address_select,data);
    }


    @Override
    protected void convert(@NonNull BaseViewHolder helper, AddressBean.Area item) {
        ((TextView)helper.getView(R.id.tv_address)).setText(item.getDistnm());
        helper.addOnClickListener(R.id.tv_address);
        if (getData().size()> 0 &&getData().get(helper.getLayoutPosition()).isClicks()){
            ((TextView) helper.getView(R.id.tv_address)).setTextColor(Color.parseColor("#76CEE1"));
        } else {
            ((TextView) helper.getView(R.id.tv_address)).setTextColor(Color.parseColor("#6D7278"));
        }
    }
}