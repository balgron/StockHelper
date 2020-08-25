package com.fox.stockhelper.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fox.stockhelper.R;
import com.fox.stockhelper.api.stock.realtime.TopIndexApi;
import com.fox.stockhelper.api.stock.realtime.UptickRateStatisticsApi;
import com.fox.stockhelper.config.MsgWhatConfig;
import com.fox.stockhelper.constant.stock.StockMarketStatusConst;
import com.fox.stockhelper.entity.dto.api.stock.realtime.TopIndexApiDto;
import com.fox.stockhelper.entity.dto.api.stock.realtime.UptickRateStatisticsApiDto;
import com.fox.stockhelper.serv.stock.StockMarketStatusServ;
import com.fox.stockhelper.ui.base.BaseFragment;
import com.fox.stockhelper.ui.handler.CommonHandler;
import com.fox.stockhelper.ui.listener.CommonHandleListener;
import com.fox.stockhelper.ui.view.StockIndexBlockView;
import com.fox.stockhelper.util.DateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.SneakyThrows;

/**
 * 股市首页
 * @author lusongsong
 */
public class StockMarketFragment extends BaseFragment implements CommonHandleListener {
    /**
     * 股市
     */
    private String stockMarket;
    /**
     * 股市状态
     */
    private int smStatus = StockMarketStatusConst.OPEN;
    /**
     * 股市状态logo
     */
    @BindView(R.id.smStatusIV)
    ImageView smStatusIV;
    /**
     * 股市状态文案
     */
    @BindView(R.id.smStatusTV)
    TextView smStatusTV;
    /**
     * 当前日期
     */
    @BindView(R.id.smDate)
    TextView smDate;
    /**
     * 星期
     */
    @BindView(R.id.smWeek)
    TextView smWeek;
    /**
     * 股票排行
     */
    @BindView(R.id.stockRankHeader)
    LinearLayout stockRankHeader;
    /**
     * top指数列表
     */
    @BindView(R.id.topIndexLL)
    LinearLayout topIndexLL;
    /**
     * 统计跌文本
     */
    @BindView(R.id.smUSDownTV)
    TextView smUSDownTV;
    /**
     * 统计跌图片
     */
    @BindView(R.id.smUSDownIV)
    ImageView smUSDownIV;
    /**
     * 统计平文本
     */
    @BindView(R.id.smUSFlatTV)
    TextView smUSFlatTV;
    /**
     * 统计平图片
     */
    @BindView(R.id.smUSFlatIV)
    ImageView smUSFlatIV;
    /**
     * 统计涨文本
     */
    @BindView(R.id.smUSUpTV)
    TextView smUSUpTV;
    /**
     * 统计涨图片
     */
    @BindView(R.id.smUSUpIV)
    ImageView smUSUpIV;
    /**
     * 统计涨停文本
     */
    @BindView(R.id.smUSUpLimitTV)
    TextView smUSUpLimitTV;
    /**
     * 统计跌停文本
     */
    @BindView(R.id.smUSDownLimitTV)
    TextView smUSDownLimitTV;
    /**
     * top指标ui
     */
    Map<Integer, StockIndexBlockView> topIndexMap = new HashMap<>();
    /**
     * 消息处理
     */
    Handler handler = new CommonHandler(this);

    /**
     * 指定上下文和股市
     * @param context
     * @param stockMarket
     */
    public StockMarketFragment(Context context, String stockMarket) {
        super(context);
        this.stockMarket = stockMarket;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_market, null);
        ButterKnife.bind(this, view);
        //显示时间
        this.initDate();
        //股市状态
        this.handleStockMarketStatus();
        //top指标
        this.handleStockMarketTopIndex();
        //涨跌统计
        this.handleStockMarketStatistics();
        return view;
    }

    /**
     * 设置显示时间
     */
    private void initDate() {
        smDate.setText(DateUtil.getCurrentDate());
        smWeek.setText(DateUtil.getCurrentWeekNum());
    }

    /**
     * 消息处理
     * @param message
     */
    @Override
    public void handleMessage(Message message) {
        Bundle bundle = message.getData();
        switch (message.what) {
            case MsgWhatConfig.SM_STATUS:
                smStatus = bundle.getInt("smStatus");
                int[] smStatusInfo = StockMarketStatusConst.getStatusInfo(smStatus);
                smStatusIV.setImageResource(smStatusInfo[0]);
                smStatusTV.setText(smStatusInfo[1]);
                if (smStatus != StockMarketStatusConst.OPEN
                        && smStatus != StockMarketStatusConst.COMPETE) {
                    smStatusTV.setTextColor(Color.BLACK);
                } else {
                    smStatusTV.setTextColor(Color.RED);
                }
                break;
            case MsgWhatConfig.TOP_INDEX:
                String[] topIndexArr = bundle.getStringArray("topIndex");
                try {
                    for (String topIndexStr : topIndexArr) {
                        TopIndexApiDto topIndexApiDto =
                                new ObjectMapper().readValue(topIndexStr, TopIndexApiDto.class);
                        int stockId = topIndexApiDto.getStockId();
                        StockIndexBlockView stockIndexBlockView;
                        if (!topIndexMap.containsKey(stockId)) {
                            stockIndexBlockView = new StockIndexBlockView(this.context);
                            stockIndexBlockView.setLayoutParams(
                                    new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            1)
                            );
                            stockIndexBlockView.setGravity(RelativeLayout.CENTER_IN_PARENT);
                            stockIndexBlockView.setName(topIndexApiDto.getStockName());
                            stockIndexBlockView.setCurrentPrice(topIndexApiDto.getCurrentPrice());
                            stockIndexBlockView.setYesterdayClosePrice(
                                    topIndexApiDto.getYesterdayClosePrice()
                            );
                            topIndexMap.put(stockId, stockIndexBlockView);
                            topIndexLL.addView(stockIndexBlockView);
                        } else {
                            stockIndexBlockView = topIndexMap.get(stockId);
                            stockIndexBlockView.setCurrentPrice(topIndexApiDto.getCurrentPrice());
                        }
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    Log.e("topIndexRuntimeException", e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("topIndexException", e.getMessage());
                }
                break;
            case MsgWhatConfig.UPTICK_RATE_STATISTICS:
                String uptickRateStatisticsStr = bundle.getString("uptickRateStatistics");
                try {
                    UptickRateStatisticsApiDto uptickRateStatisticsApiDto =
                            new ObjectMapper().readValue(
                                    uptickRateStatisticsStr,
                                    UptickRateStatisticsApiDto.class
                            );
                    smUSDownTV.setText(String.valueOf(uptickRateStatisticsApiDto.getDown()));
                    smUSDownLimitTV.setText(String.valueOf(uptickRateStatisticsApiDto.getDownLimit()));
                    smUSFlatTV.setText(String.valueOf(uptickRateStatisticsApiDto.getFlat()));
                    smUSUpTV.setText(String.valueOf(uptickRateStatisticsApiDto.getUp()));
                    smUSUpLimitTV.setText(String.valueOf(uptickRateStatisticsApiDto.getUpLimit()));
                    smUSDownIV.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            uptickRateStatisticsApiDto.getDown()
                    ));
                    smUSFlatIV.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            uptickRateStatisticsApiDto.getFlat()
                    ));
                    smUSUpIV.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            uptickRateStatisticsApiDto.getUp()
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("uptickRateStatisticsException", e.getMessage());
                }
                break;
        }
    }

    /**
     * 开启定时检查交易状态
     */
    private void handleStockMarketStatus() {
        Runnable stockMarketStatusRunnable = new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while (true) {
                    int smStatus = StockMarketStatusServ.getStockMarketStatus();
                    Message msg = new Message();
                    msg.what = MsgWhatConfig.SM_STATUS;
                    Bundle bundle = new Bundle();
                    bundle.putInt("smStatus", smStatus);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    int minute = Integer.valueOf(DateUtil.getCurrentDate(DateUtil.MINUTE_FORMAT_1));
                    int second = Integer.valueOf(DateUtil.getCurrentDate(DateUtil.SECOND_FORMAT_1));
                    int s = 300 - second - (minute % 5) * 60;
                    Thread.sleep(s * 1000);
                }
            }
        };
        new Thread(stockMarketStatusRunnable).start();
    }

    /**
     * 处理涨跌统计
     */
    private void handleStockMarketStatistics() {
        Runnable stockMarketStatusRunnable = new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while (true) {
                    if (smStatus == StockMarketStatusConst.OPEN
                            || smStatus == StockMarketStatusConst.COMPETE) {
                        TopIndexApi topIndexApi = new TopIndexApi();
                        List<TopIndexApiDto> list = (List<TopIndexApiDto>)topIndexApi.request();
                        String[] topIndexArr = new String[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            topIndexArr[i] = JSONObject.toJSONString(list.get(i));
                        }
                        Message msg = new Message();
                        msg.what = MsgWhatConfig.TOP_INDEX;
                        Bundle bundle = new Bundle();
                        bundle.putStringArray("topIndex", topIndexArr);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                    Thread.sleep(3000);
                }
            }
        };
        Thread thread = new Thread(stockMarketStatusRunnable);
        thread.start();
    }

    /**
     * 处理顶部指指数
     */
    private void handleStockMarketTopIndex() {
        Runnable stockMarketStatisticsRunnable = new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while (true) {
                    if (smStatus == StockMarketStatusConst.OPEN
                            || smStatus == StockMarketStatusConst.COMPETE) {
                        UptickRateStatisticsApi uptickRateStatisticsApi =
                                new UptickRateStatisticsApi();
                        UptickRateStatisticsApiDto uptickRateStatisticsApiDto =
                                (UptickRateStatisticsApiDto)uptickRateStatisticsApi.request();
                        Message msg = new Message();
                        msg.what = MsgWhatConfig.UPTICK_RATE_STATISTICS;
                        Bundle bundle = new Bundle();
                        bundle.putString(
                                "uptickRateStatistics",
                                JSONObject.toJSONString(uptickRateStatisticsApiDto)
                        );
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                    Thread.sleep(3000);
                }
            }
        };
        Thread thread = new Thread(stockMarketStatisticsRunnable);
        thread.start();
    }
}
