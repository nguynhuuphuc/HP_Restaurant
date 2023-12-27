package com.example.hprestaurant;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.hprestaurant.Adapters.MenuAdapter;
import com.example.hprestaurant.Adapters.MenuCategoryAdapter;
import com.example.hprestaurant.BottomSheetDialogFragment.NoteProductFragment;
import com.example.hprestaurant.Interfaces.IClickItemMenuCategoryListener;
import com.example.hprestaurant.Interfaces.IClickViewInNoteProductListeners;
import com.example.hprestaurant.Interfaces.IItemMenu;
import com.example.hprestaurant.Models.DishModel;
import com.example.hprestaurant.Models.MenuCategoryModel;
import com.example.hprestaurant.Models.MenuModel;
import com.example.hprestaurant.Models.OrderItemModel;
import com.example.hprestaurant.Models.ProductInReservationModel;
import com.example.hprestaurant.Models.Request.OrderRequest;
import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.Utils.Auth;
import com.example.hprestaurant.Utils.CircleAnimationUtil;
import com.example.hprestaurant.databinding.ActivityDishesSelectionBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.wasabeef.glide.transformations.BlurTransformation;
import render.animations.Render;
import render.animations.Slide;

public class DishesSelectionActivity extends AppCompatActivity {
    private ActivityDishesSelectionBinding binding;
    private int action;
    private List<MenuCategoryModel> menuCategoryModelList;
    private MenuCategoryAdapter menuCategoryAdapter;
    private String typeId = "00";
    private List<MenuModel> menuModelsList;
    private  MenuAdapter menuAdapter;
    private List<DishModel> dishes = new ArrayList<>();
    private Boolean alreadyUp = false;
    private Render render = new Render(this);
    private FirebaseFirestore db;
    private MyApplication mApp;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDishesSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        action = getIntent().getIntExtra("action",-1);
        db = FirebaseFirestore.getInstance();
        mApp = (MyApplication) getApplication();
        registerLauncher();
        if(action == Action.DISHES_AND_TABLE){
            viewDishesThenTable();
        }else if(action == Action.ONLY_DISHES){
            viewOnlyDishes();
        }
        setRv();
        passData();

        binding.toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.reSelectCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAndPopDown();
                menuAdapter.getFilter().filter(typeId);
            }
        });

        binding.addToTableCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderDishes orderDishes = getOrderItems();
                OrderRequest orderRequest = new OrderRequest(orderDishes.getOrderItemModels(),
                        mApp.getCustomer().getCustomer_id(),
                        -1,
                        orderDishes.getTotal_amount(),
                        "",
                        0);
                Intent intent;
                if(action==Action.DISHES_AND_TABLE){
                    intent = new Intent(DishesSelectionActivity.this,BookingActivity.class);
                    intent.putExtra("orderRequest",orderRequest);
                    intent.putExtra("action",Action.DISHES_SELECTED);
                    activityLauncher(intent);
                }else if(action == Action.ONLY_DISHES){
                    intent = new Intent(DishesSelectionActivity.this,DateTimeSelectedActivity.class);
                    intent.putExtra("orderRequest",orderRequest);
                    activityLauncher(intent);
                }
            }
        });
        
    }

    private void activityLauncher(Intent intent){
        launcher.launch(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.no_animation);
    }
    private void registerLauncher() {
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    assert data != null;
                    int action = data.getIntExtra("action",-1);
                    if(action == Action.BOOKING_SUCCESS){
                        setResult(RESULT_OK,data);
                        finish();
                    }
                }
            }
        });
    }

    private void inProgress(boolean b) {
        if(b){
            binding.allMenuLl.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            return;
        }
        binding.allMenuLl.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);

    }
    private void inProgressItem(boolean b) {
        if(b){
            binding.itemInMenuRv.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            return;
        }
        binding.itemInMenuRv.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);

    }
    private void passData() {
        inProgress(true);
        db.collection("menucategory")
                .orderBy("id", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                MenuCategoryModel model = document.toObject(MenuCategoryModel.class);
                                menuCategoryModelList.add(0,model);
                            }
                            menuCategoryAdapter.notifyDataSetChanged();
                            inProgress(false);
                        }
                    }
                });
        inProgressItem(true);
        db.collection("menu")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document: task.getResult()){
                                MenuModel model = document.toObject(MenuModel.class);
                                model.setDocumentId(document.getId());
                                alreadyAddToTable(model);
                                menuModelsList.add(model);
                            }
                            menuAdapter.notifyDataSetChanged();
                            inProgressItem(false);
                        }
                    }
                });
    }
    private void alreadyAddToTable(MenuModel model) {
        for(DishModel vt : dishes){
            if(vt.getDocumentId().equals(model.getDocumentId())){
                model.setSelected(true);
                model.setAdded(true);
            }
        }
    }


    private void setRv() {
        binding.firstMenuCategoryRv.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        RecyclerView.HORIZONTAL,
                        false
                )
        );
        menuCategoryModelList = new ArrayList<>();
        menuCategoryAdapter = new MenuCategoryAdapter(menuCategoryModelList, new IClickItemMenuCategoryListener() {
            @Override
            public void onClickItemMenuCategoryListener(MenuCategoryModel model) {
                typeId = model.getId();
                menuAdapter.getFilter().filter(typeId);
            }
        });
        binding.firstMenuCategoryRv.setAdapter(menuCategoryAdapter);


        binding.itemInMenuRv.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        RecyclerView.VERTICAL,
                        false));

        menuModelsList = new ArrayList<>();

        menuAdapter = new MenuAdapter(menuModelsList,this, new IItemMenu() {
            @Override
            public void loadImgItem(MenuModel models, ImageView imgV, LinearLayout quantityLl, ImageView checkIv) {
                if(models.isSelected() && !models.isAdded()){
                    new CircleAnimationUtil().attachActivity(DishesSelectionActivity.this)
                            .setTargetView(imgV).setMoveDuration(1000).setDestView(binding.numberDifferentProductTv)
                            .setAnimationListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(@NonNull Animator animation) {
                                    quantityLl.setVisibility(View.VISIBLE);
                                    checkIv.setVisibility(View.VISIBLE);
                                    imgV.setVisibility(View.VISIBLE);
                                    Glide.with(DishesSelectionActivity.this)
                                            .load(models.getImg())
                                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                                            .into(imgV);
                                }

                                @Override
                                public void onAnimationEnd(@NonNull Animator animation) {
                                    binding.numberDifferentProductTv.setText(String.valueOf(dishes.size()));


                                }

                                @Override
                                public void onAnimationCancel(@NonNull Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(@NonNull Animator animation) {

                                }
                            }).startAnimation();
                    return;
                }
                if(checkIv != null) checkIv.setVisibility(View.GONE);
                if(!models.isAdded()) Glide.with(DishesSelectionActivity.this).load(models.getImg()).into(imgV);
            }

            @Override
            public void onClickItemMenuListener(MenuModel models, int position) {
                viewSelectAddToTable(models.isSelected());
                if(models.isSelected() && !models.isAdded()){
                    addToTable(models);
                    menuAdapter.notifyItemChanged(position);
                    return;
                }
                DishModel dishModel = null;
                for(DishModel vt : dishes){
                    if(vt.getDocumentId().equals(models.getDocumentId())){
                        dishModel = vt;
                    }
                }
                NoteProductFragment fragment = new NoteProductFragment(models, dishModel, new IClickViewInNoteProductListeners() {
                    @Override
                    public void onClickListener(ProductInReservationModel pModel, String command) {
                        switch (command){
                            case "remove":
                                removeTable(models);
                                if (dishes.size() == 0 && alreadyUp){
                                    clearAndPopDown();
                                }
                                menuAdapter.getFilter().filter(typeId);
                                break;
                            case "saved":
                                menuAdapter.setVirtualTableList(dishes);
                                menuAdapter.notifyItemChanged(position);
                        }

                    }
                });
                fragment.show(getSupportFragmentManager(),fragment.getTag());



            }

            @Override
            public void onClickPlusListener(MenuModel models,int value) {
                for(DishModel vt : dishes){
                    if(vt.getDocumentId().equals(models.getDocumentId())){
                        vt.setQuantity(String.valueOf(value));
                    }
                }
            }

            @Override
            public void onClickMinusListener(MenuModel models, String signal,int value) {
                if(signal.equals("delete")){
                    removeTable(models);
                    binding.numberDifferentProductTv.setText(String.valueOf(dishes.size()));
                }
                else{
                    for(DishModel vt : dishes){
                        if(vt.getDocumentId().equals(models.getDocumentId())){
                            vt.setQuantity(String.valueOf(value));
                        }
                    }
                }
                if (dishes.size() == 0 && alreadyUp){
                    clearAndPopDown();
                }

            }

        });


        binding.itemInMenuRv.setAdapter(menuAdapter);
    }
    private void addToTable(MenuModel models) {
        models.setAdded(true);
        DishModel model = new DishModel(models.getDocumentId(),"1",models.getPrice());
        model.setOder_time(new Date());
        model.setPrice(models.getPrice());
        dishes.add(model);
    }
    private void removeTable(MenuModel models) {
        models.setSelected(false);
        models.setAdded(false);
        dishes.removeIf(vT -> vT.getDocumentId().equals(models.getDocumentId()));
    }
    private void viewSelectAddToTable(boolean b){
        if(b && !alreadyUp){
            menuAdapter.setVirtualTableList(dishes);
            alreadyUp = true;
            binding.reSelectCv.setVisibility(View.VISIBLE);
            binding.addToTableCv.setVisibility(View.VISIBLE);
            render.setAnimation(Slide.InUp(binding.reSelectCv));
            render.start();
            render.setAnimation(Slide.InUp(binding.addToTableCv));
            render.start();
            return;
        }
    }
    private void clearAndPopDown() {
        if(dishes.size()!= 0){
            for(DishModel dish : dishes){
                for(MenuModel model : menuModelsList){
                    if(model.getDocumentId().equals(dish.getDocumentId())){
                        model.setSelected(false);
                        model.setAdded(false);
                    }
                }
            }
            dishes.clear();
        }
        binding.numberDifferentProductTv.setText(String.valueOf(dishes.size()));
        alreadyUp = false;
        render.setAnimation(Slide.OutDown(binding.reSelectCv));
        render.start();
        render.setAnimation(Slide.OutDown(binding.addToTableCv));
        render.start();
    }
    private class OrderDishes{
        private List<OrderItemModel> orderItemModels;
        private double total_amount;

        public OrderDishes(List<OrderItemModel> orderItemModels, double total_amount) {
            this.orderItemModels = orderItemModels;
            this.total_amount = total_amount;
        }

        public List<OrderItemModel> getOrderItemModels() {
            return orderItemModels;
        }

        public void setOrderItemModels(List<OrderItemModel> orderItemModels) {
            this.orderItemModels = orderItemModels;
        }

        public double getTotal_amount() {
            return total_amount;
        }

        public void setTotal_amount(double total_amount) {
            this.total_amount = total_amount;
        }
    }
    private OrderDishes getOrderItems() {
        inProgressItem(true);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator(',');

        long vtQuanity = 0;
        double totalPrice = 0;

        final Map<String,Object> orderMap = new HashMap<>();
        List<OrderItemModel> orderItemModels = new ArrayList<>();

        for(DishModel vt : dishes){
            OrderItemModel model = new OrderItemModel();
            vtQuanity = Long.parseLong(vt.getQuantity());
            double price= Double.parseDouble(vt.getTotalPriceProduct().trim().replace(",",""))* vtQuanity;
            model.setMenu_item_id(vt.getDocumentId());
            model.setQuantity(Integer.parseInt(vt.getQuantity()));
            model.setItem_price(Double.parseDouble(vt.getPrice().replace(",","")));
            model.setNote(vt.getNote());
            model.setOrder_id(-1);
            model.setOrder_time(vt.getOder_time());



            if(!vt.getValueDiscount().isEmpty()){
                if(vt.isDiscountUnit()){
                    model.setDiscount_percentage(Integer.parseInt(vt.getValueDiscount()));
                    price = price -  (price * model.getDiscount_percentage()/100);
                }
                else{
                    model.setDiscount_amount(Double.parseDouble(vt.getValueDiscount().replace(",","")));
                    price = price - (model.getDiscount_amount() * vtQuanity);
                }
            }
            totalPrice += price;

            orderItemModels.add(model);
        }
        inProgressItem(false);
        return new OrderDishes(orderItemModels,totalPrice);
    }



    private void viewOnlyDishes() {
    }

    private void viewDishesThenTable() {
    }
}