package com.fox.stockhelper.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fox.stockhelper.R;
import com.fox.stockhelper.api.stock.realtime.DealInfoApi;
import com.fox.stockhelper.api.stock.realtime.DealPriceLineApi;
import com.fox.stockhelper.config.MsgWhatConfig;
import com.fox.stockhelper.constant.stock.StockMarketStatusConst;
import com.fox.stockhelper.entity.dto.api.stock.realtime.DealInfoApiDto;
import com.fox.stockhelper.entity.dto.api.stock.realtime.DealPriceLineApiDto;
import com.fox.stockhelper.serv.stock.StockMarketStatusServ;
import com.fox.stockhelper.ui.adapter.recyclerview.StockTopDealPriceAdapter;
import com.fox.stockhelper.ui.base.BaseFragment;
import com.fox.stockhelper.ui.handler.CommonHandler;
import com.fox.stockhelper.ui.listener.CommonHandleListener;
import com.fox.stockhelper.ui.view.StockDealInfoView;
import com.fox.stockhelper.util.DateUtil;
import com.github.mikephil.charting.stockChart.OneDayChart;
import com.github.mikephil.charting.stockChart.dataManage.TimeDataManage;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.SneakyThrows;

/**
 * 股市实时成交线图
 * @author lusongsong
 * @date 2020/9/14 15:57
 */
public class StockDealLineRealtimeFragment extends BaseFragment implements CommonHandleListener {
    /**
     * 图标测试数据
     */
    public static final String TIMEDATA = "{\"data\":[[1528162200000,\"3087.1837\",\"3087.9869\",\"1949810.0\",3088.0076],[1528162260000,\"3084.3243\",\"3087.3451\",\"1081702.0\",3088.0076],[1528162320000,\"3085.4288\",\"3086.86\",\"978263.0\",3088.0076],[1528162380000,\"3086.3579\",\"3086.7958\",\"933516.0\",3088.0076],[1528162440000,\"3086.0155\",\"3086.6825\",\"999550.0\",3088.0076],[1528162500000,\"3085.2891\",\"3086.5499\",\"1025168.0\",3088.0076],[1528162560000,\"3084.6777\",\"3086.3875\",\"910903.0\",3088.0076],[1528162620000,\"3085.079\",\"3086.2727\",\"822856.0\",3088.0076],[1528162680000,\"3090.1321\",\"3086.4364\",\"1066190.0\",3088.0076],[1528162740000,\"3091.6931\",\"3086.7703\",\"977087.0\",3088.0076],[1528162800000,\"3091.9409\",\"3087.1237\",\"939072.0\",3088.0076],[1528162860000,\"3089.6527\",\"3087.3021\",\"826022.0\",3088.0076],[1528162920000,\"3089.6625\",\"3087.4514\",\"880274.0\",3088.0076],[1528162980000,\"3089.0147\",\"3087.5467\",\"799578.0\",3088.0076],[1528163040000,\"3087.2929\",\"3087.568\",\"794711.0\",3088.0076],[1528163100000,\"3086.2263\",\"3087.5286\",\"684310.0\",3088.0076],[1528163160000,\"3086.392\",\"3087.4523\",\"769528.0\",3088.0076],[1528163220000,\"3086.3709\",\"3087.4027\",\"682865.0\",3088.0076],[1528163280000,\"3085.8989\",\"3087.3517\",\"604684.0\",3088.0076],[1528163340000,\"3085.802\",\"3087.2985\",\"617549.0\",3088.0076],[1528163400000,\"3086.49\",\"3087.2479\",\"666819.0\",3088.0076],[1528163460000,\"3089.164\",\"3087.2577\",\"710830.0\",3088.0076],[1528163520000,\"3090.1754\",\"3087.3374\",\"677791.0\",3088.0076],[1528163580000,\"3090.1085\",\"3087.4152\",\"645861.0\",3088.0076],[1528163640000,\"3087.3565\",\"3087.4595\",\"688077.0\",3088.0076],[1528163700000,\"3088.1053\",\"3087.468\",\"581501.0\",3088.0076],[1528163760000,\"3087.6867\",\"3087.4769\",\"576744.0\",3088.0076],[1528163820000,\"3087.5904\",\"3087.4814\",\"687473.0\",3088.0076],[1528163880000,\"3088.2759\",\"3087.4904\",\"583178.0\",3088.0076],[1528163940000,\"3088.4094\",\"3087.5106\",\"627352.0\",3088.0076],[1528164000000,\"3089.3992\",\"3087.5399\",\"582951.0\",3088.0076],[1528164060000,\"3088.6242\",\"3087.5767\",\"665360.0\",3088.0076],[1528164120000,\"3087.1677\",\"3087.5728\",\"724730.0\",3088.0076],[1528164180000,\"3086.8108\",\"3087.5559\",\"657465.0\",3088.0076],[1528164240000,\"3087.3281\",\"3087.5426\",\"550718.0\",3088.0076],[1528164300000,\"3087.3292\",\"3087.5356\",\"502531.0\",3088.0076],[1528164360000,\"3088.573\",\"3087.5327\",\"499184.0\",3088.0076],[1528164420000,\"3088.5262\",\"3087.5408\",\"474261.0\",3088.0076],[1528164480000,\"3088.6579\",\"3087.5548\",\"448144.0\",3088.0076],[1528164540000,\"3087.8702\",\"3087.5604\",\"526101.0\",3088.0076],[1528164600000,\"3086.7743\",\"3087.5518\",\"496864.0\",3088.0076],[1528164660000,\"3085.3214\",\"3087.5238\",\"595431.0\",3088.0076],[1528164720000,\"3085.0465\",\"3087.4785\",\"627486.0\",3088.0076],[1528164780000,\"3084.5606\",\"3087.4384\",\"501423.0\",3088.0076],[1528164840000,\"3084.4522\",\"3087.3898\",\"522625.0\",3088.0076],[1528164900000,\"3083.5785\",\"3087.3253\",\"567207.0\",3088.0076],[1528164960000,\"3083.012\",\"3087.2617\",\"558404.0\",3088.0076],[1528165020000,\"3084.1331\",\"3087.2044\",\"535789.0\",3088.0076],[1528165080000,\"3084.5716\",\"3087.1693\",\"446820.0\",3088.0076],[1528165140000,\"3083.6961\",\"3087.1334\",\"453272.0\",3088.0076],[1528165200000,\"3083.6464\",\"3087.0889\",\"464542.0\",3088.0076],[1528165260000,\"3082.9999\",\"3087.0455\",\"431374.0\",3088.0076],[1528165320000,\"3084.3031\",\"3087.0029\",\"424519.0\",3088.0076],[1528165380000,\"3083.664\",\"3086.9637\",\"450735.0\",3088.0076],[1528165440000,\"3084.5678\",\"3086.9344\",\"411306.0\",3088.0076],[1528165500000,\"3084.4452\",\"3086.9103\",\"362652.0\",3088.0076],[1528165560000,\"3084.8084\",\"3086.8855\",\"409901.0\",3088.0076],[1528165620000,\"3084.1758\",\"3086.8567\",\"436077.0\",3088.0076],[1528165680000,\"3084.4639\",\"3086.828\",\"439142.0\",3088.0076],[1528165740000,\"3084.2586\",\"3086.805\",\"389209.0\",3088.0076],[1528165800000,\"3084.1004\",\"3086.7751\",\"428571.0\",3088.0076],[1528165860000,\"3083.0136\",\"3086.7428\",\"400146.0\",3088.0076],[1528165920000,\"3082.9311\",\"3086.7052\",\"408753.0\",3088.0076],[1528165980000,\"3082.0337\",\"3086.6527\",\"477250.0\",3088.0076],[1528166040000,\"3081.3699\",\"3086.5908\",\"524087.0\",3088.0076],[1528166100000,\"3080.7869\",\"3086.5341\",\"439671.0\",3088.0076],[1528166160000,\"3081.2286\",\"3086.4767\",\"427287.0\",3088.0076],[1528166220000,\"3081.8209\",\"3086.4323\",\"460691.0\",3088.0076],[1528166280000,\"3082.9183\",\"3086.3997\",\"358174.0\",3088.0076],[1528166340000,\"3083.4949\",\"3086.3781\",\"313333.0\",3088.0076],[1528166400000,\"3083.7834\",\"3086.3559\",\"349953.0\",3088.0076],[1528166460000,\"3083.4668\",\"3086.3327\",\"362756.0\",3088.0076],[1528166520000,\"3083.737\",\"3086.3136\",\"326455.0\",3088.0076],[1528166580000,\"3084.6622\",\"3086.2961\",\"347599.0\",3088.0076],[1528166640000,\"3084.7524\",\"3086.2819\",\"376214.0\",3088.0076],[1528166700000,\"3085.4348\",\"3086.2727\",\"353256.0\",3088.0076],[1528166760000,\"3085.6831\",\"3086.2649\",\"355852.0\",3088.0076],[1528166820000,\"3085.6151\",\"3086.2589\",\"331776.0\",3088.0076],[1528166880000,\"3086.096\",\"3086.255\",\"311030.0\",3088.0076],[1528166940000,\"3085.8002\",\"3086.2503\",\"379497.0\",3088.0076],[1528167000000,\"3085.4789\",\"3086.2455\",\"301428.0\",3088.0076],[1528167060000,\"3085.7348\",\"3086.2407\",\"293185.0\",3088.0076],[1528167120000,\"3085.4226\",\"3086.2362\",\"281122.0\",3088.0076],[1528167180000,\"3085.7692\",\"3086.2327\",\"310075.0\",3088.0076],[1528167240000,\"3086.1626\",\"3086.2283\",\"311789.0\",3088.0076],[1528167300000,\"3085.9496\",\"3086.2262\",\"316722.0\",3088.0076],[1528167360000,\"3086.6105\",\"3086.2252\",\"311435.0\",3088.0076],[1528167420000,\"3088.6915\",\"3086.2375\",\"411297.0\",3088.0076],[1528167480000,\"3090.5059\",\"3086.2679\",\"437148.0\",3088.0076],[1528167540000,\"3090.5759\",\"3086.3023\",\"416667.0\",3088.0076],[1528167600000,\"3091.2854\",\"3086.3412\",\"420083.0\",3088.0076],[1528167660000,\"3092.8804\",\"3086.3876\",\"424797.0\",3088.0076],[1528167720000,\"3092.9012\",\"3086.4342\",\"383137.0\",3088.0076],[1528167780000,\"3093.3555\",\"3086.4855\",\"388370.0\",3088.0076],[1528167840000,\"3093.3592\",\"3086.5334\",\"356904.0\",3088.0076],[1528167900000,\"3093.5546\",\"3086.583\",\"359207.0\",3088.0076],[1528167960000,\"3093.0045\",\"3086.6319\",\"383456.0\",3088.0076],[1528168020000,\"3094.1247\",\"3086.6726\",\"312177.0\",3088.0076],[1528168080000,\"3095.0461\",\"3086.7223\",\"349647.0\",3088.0076],[1528168140000,\"3096.1459\",\"3086.7742\",\"336969.0\",3088.0076],[1528168200000,\"3096.0777\",\"3086.8255\",\"320881.0\",3088.0076],[1528168260000,\"3092.7287\",\"3086.8777\",\"384580.0\",3088.0076],[1528168320000,\"3093.1134\",\"3086.9183\",\"405922.0\",3088.0076],[1528168380000,\"3092.9472\",\"3086.9451\",\"279527.0\",3088.0076],[1528168440000,\"3092.1235\",\"3086.9752\",\"331639.0\",3088.0076],[1528168500000,\"3091.9735\",\"3087.0019\",\"309056.0\",3088.0076],[1528168560000,\"3092.4459\",\"3087.0269\",\"306053.0\",3088.0076],[1528168620000,\"3091.78\",\"3087.0473\",\"268435.0\",3088.0076],[1528168680000,\"3092.151\",\"3087.0692\",\"252867.0\",3088.0076],[1528168740000,\"3092.3638\",\"3087.0912\",\"244529.0\",3088.0076],[1528168800000,\"3092.2221\",\"3087.1123\",\"235287.0\",3088.0076],[1528168860000,\"3091.8891\",\"3087.1332\",\"256178.0\",3088.0076],[1528168920000,\"3091.5449\",\"3087.1534\",\"260421.0\",3088.0076],[1528168980000,\"3091.8459\",\"3087.1732\",\"243489.0\",3088.0076],[1528169040000,\"3092.5219\",\"3087.2009\",\"317702.0\",3088.0076],[1528169100000,\"3093.7043\",\"3087.2317\",\"298768.0\",3088.0076],[1528169160000,\"3094.5779\",\"3087.2723\",\"339856.0\",3088.0076],[1528169220000,\"3095.8277\",\"3087.3144\",\"319506.0\",3088.0076],[1528169280000,\"3096.5076\",\"3087.3736\",\"388945.0\",3088.0076],[1528169340000,\"3098.3938\",\"3087.4311\",\"370382.0\",3088.0076]],\"preClose\":\"3091.19\"}";
    /**
     * 交易信息
     */
    @BindView(R.id.stockDealInfoSDIV)
    StockDealInfoView stockDealInfoSDIV;
    /**
     * 单天分钟线图
     */
    @BindView(R.id.stockOneDayChart)
    OneDayChart stockOneDayChart;
    /**
     * Top售价
     */
    @BindView(R.id.sellStockTopDealPriceRV)
    RecyclerView sellStockTopDealPriceRV;
    /**
     * Top买价
     */
    @BindView(R.id.buyStockTopDealPriceRV)
    RecyclerView buyStockTopDealPriceRV;
    /**
     * 股票id
     */
    private Integer stockId;
    /**
     * Top售价适配器
     */
    StockTopDealPriceAdapter sellStockTopDealPriceAdapter;
    /**
     * Top买价适配器
     */
    StockTopDealPriceAdapter buyStockTopDealPriceAdapter;
    /**
     * 是否为横屏
     */
    private boolean land = false;//是否横屏
    /**
     * 成交分时数据
     */
    private TimeDataManage kTimeData = new TimeDataManage();
    private JSONObject object;
    int smStatus = StockMarketStatusConst.OPEN;
    /**
     * 消息处理
     */
    Handler handler = new CommonHandler(this);

    /**
     * 构造函数
     * @param context
     */
    public StockDealLineRealtimeFragment(Context context) {
        super(context);
    }

    public StockDealLineRealtimeFragment(Context context, int stockId) {
        super(context);
        this.stockId = stockId;
    }

    /**
     * 创建视图
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_deal_line_realtime, null);
        ButterKnife.bind(this, view);
        //初始化
        stockOneDayChart.initChart(land);
        //更新股市状态
        this.handleStockMarketStatus();
        //初始化交易信息
        this.handleDealInfo();
        //初始化交易价格线图信息
        this.handleDealPriceLine();
        //初始化TOP售价
        sellStockTopDealPriceAdapter = new StockTopDealPriceAdapter();
        LinearLayoutManager sellLinearLayoutManager = new LinearLayoutManager(this.getContext());
        sellLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        sellStockTopDealPriceRV.setLayoutManager(sellLinearLayoutManager);
        //初始化TOP买价
        buyStockTopDealPriceAdapter = new StockTopDealPriceAdapter();
        buyStockTopDealPriceAdapter.setPriceType(StockTopDealPriceAdapter.TOP_PRICE_TYPE_BUY);
        LinearLayoutManager buyLinearLayoutManager = new LinearLayoutManager(this.getContext());
        buyLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        buyStockTopDealPriceRV.setLayoutManager(buyLinearLayoutManager);

        return view;
    }

    /**
     * 消息处理
     *
     * @param message
     */
    @Override
    public void handleMessage(Message message) {
        Bundle bundle = message.getData();
        switch (message.what) {
            case MsgWhatConfig.SM_STATUS:
                smStatus = bundle.getInt("smStatus");
                break;
            case MsgWhatConfig.STOCK_DEAL_INFO:
                String stockDealInfoStr = bundle.getString("stockDealInfo");
                try {
                    DealInfoApiDto dealInfoApiDto =
                            new ObjectMapper().readValue(stockDealInfoStr, DealInfoApiDto.class);
                    //交易信息
                    stockDealInfoSDIV.setData(dealInfoApiDto).reDraw();
                    //TOP售价
                    sellStockTopDealPriceAdapter.clearData();
                    sellStockTopDealPriceAdapter.setYesterdayClosePrice(dealInfoApiDto.getYesterdayClosePrice());
                    sellStockTopDealPriceAdapter.addData(dealInfoApiDto.getSellPriceList());
                    sellStockTopDealPriceRV.setAdapter(sellStockTopDealPriceAdapter);
                    //TOP买价
                    buyStockTopDealPriceAdapter.clearData();
                    buyStockTopDealPriceAdapter.setYesterdayClosePrice(dealInfoApiDto.getYesterdayClosePrice());
                    buyStockTopDealPriceAdapter.addData(dealInfoApiDto.getBuyPriceList());
                    buyStockTopDealPriceRV.setAdapter(buyStockTopDealPriceAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case MsgWhatConfig.STOCK_DEAL_PRICE_LINE:
                String dealPriceLineApiDtoStr = bundle.getString("stockDealPriceLine");
                try {
                    DealPriceLineApiDto dealPriceLineApiDto =
                            new ObjectMapper()
                                    .readValue(dealPriceLineApiDtoStr, DealPriceLineApiDto.class);
                    object = new JSONObject(com.alibaba.fastjson.JSONObject.toJSONString(dealPriceLineApiDto.convertToRealTimeChartData()));
                    Log.e("StockDealLineRealtimeFragment", com.alibaba.fastjson.JSONObject.toJSONString(dealPriceLineApiDto.convertToRealTimeChartData()));
                    //上证指数代码000001.IDX.SH
                    kTimeData.parseTimeData(object,"000001.IDX.SH",0);
                    stockOneDayChart.setDataToChart(kTimeData);
                } catch (Exception e) {
                    e.printStackTrace();
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
     * 刷新交易信息
     */
    private void handleDealInfo() {
        Runnable stockDealInfoRunnable = new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while (true) {
                    if (smStatus == StockMarketStatusConst.OPEN
                            || smStatus == StockMarketStatusConst.COMPETE
                    ) {
                        DealInfoApi dealInfoApi = new DealInfoApi();
                        Map<String, Object> params = new HashMap<>();
                        params.put("stockId", stockId);
                        dealInfoApi.setParams(params);
                        DealInfoApiDto dealInfoApiDto = (DealInfoApiDto)dealInfoApi.request();
                        Message msg = new Message();
                        msg.what = MsgWhatConfig.STOCK_DEAL_INFO;
                        Bundle bundle = new Bundle();
                        bundle.putString("stockDealInfo",
                                com.alibaba.fastjson.JSONObject.toJSONString(dealInfoApiDto)
                        );
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        Thread.sleep(3000);
                    }
                }
            }
        };
        Thread thread = new Thread(stockDealInfoRunnable);
        thread.start();
    }

    /**
     * 刷新交易价格线图信息
     */
    private void handleDealPriceLine() {
        Runnable stockDealPriceLineRunnable = new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while (true) {
                    if (smStatus == StockMarketStatusConst.OPEN
                            || smStatus == StockMarketStatusConst.COMPETE
                    ) {
                        DealPriceLineApi dealPriceLineApi = new DealPriceLineApi();
                        Map<String, Object> params = new HashMap<>();
                        params.put("stockId", stockId);
                        dealPriceLineApi.setParams(params);
                        DealPriceLineApiDto dealPriceLineApiDto = (DealPriceLineApiDto)dealPriceLineApi.request();
                        Message msg = new Message();
                        msg.what = MsgWhatConfig.STOCK_DEAL_PRICE_LINE;
                        Bundle bundle = new Bundle();
                        bundle.putString("stockDealPriceLine",
                                com.alibaba.fastjson.JSONObject.toJSONString(dealPriceLineApiDto)
                        );
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        Thread.sleep(3000);
                    }
                }
            }
        };
        Thread thread = new Thread(stockDealPriceLineRunnable);
        thread.start();
    }
}
