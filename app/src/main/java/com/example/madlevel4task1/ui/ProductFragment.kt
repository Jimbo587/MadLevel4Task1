package com.example.madlevel4task1.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madlevel4task1.R
import com.example.madlevel4task1.model.Product
import com.example.madlevel4task1.repository.ProductRepository
import kotlinx.android.synthetic.main.product_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ProductFragment : Fragment() {
    private lateinit var productRepository: ProductRepository
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private val products = arrayListOf<Product>()
    private val productAdapter = ProductAdapter(products)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.product_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productRepository = ProductRepository(requireContext())

        getProductFromDatabase()

        initRv()

        addFab.setOnClickListener {
            showAddProductdialog()
        }

        deleteFab.setOnClickListener {
            removeAllProducts()
        }

    }

    private fun initRv() {

        // Initialize the recycler view with a linear layout manager, adapter
        rvShoppingList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvShoppingList.adapter = productAdapter
        rvShoppingList.setHasFixedSize(true)
        rvShoppingList.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        createItemTouchHelper().attachToRecyclerView(rvShoppingList)
    }

    @SuppressLint("InflateParams")
    private fun showAddProductdialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.add_product_dialog_title))
        val dialogLayout = layoutInflater.inflate(R.layout.add_product, null)
        val productName = dialogLayout.findViewById<EditText>(R.id.txt_product)
        val amount = dialogLayout.findViewById<EditText>(R.id.txt_amount)

        builder.setView(dialogLayout)
        builder.setPositiveButton(R.string.dialog_ok_btn) { _: DialogInterface, _: Int ->
            addProduct(productName, amount)
        }
        builder.show()
    }

    private fun addProduct(txtProductName: EditText, txtAmount: EditText) {
        if (validateFields(txtProductName, txtAmount)) {
            mainScope.launch {
                val product = Product(
                    product = txtProductName.text.toString(),
                    amount = txtAmount.text.toString().toShort()
                )

                withContext(Dispatchers.IO) {
                    productRepository.insertProduct(product)
                }

                getProductFromDatabase()
            }
        }
    }

    private fun validateFields(
        txtProductName: EditText, txtAmount: EditText
    ): Boolean {
        return if (txtProductName.text.toString().isNotBlank()
            && txtAmount.text.toString().isNotBlank()
        ) {
            true
        } else {
            Toast.makeText(activity, "Please fill in the fields", Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {

        // Callback which is used to create the ItemTouch helper. Only enables left swipe.
        // Use ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) to also enable right swipe.
        val callback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            // Enables or Disables the ability to move items up and down.
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            // Callback triggered when a user swiped an item.
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val productToDelete = products[position]
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        productRepository.deleteProduct(productToDelete)
                    }
                    getProductFromDatabase()
                }


            }
        }
        return ItemTouchHelper(callback)
    }

    private fun getProductFromDatabase() {
        mainScope.launch {
            val shoppingList = withContext(Dispatchers.IO) {
                productRepository.getAllProducts()
            }
            this@ProductFragment.products.clear()
            this@ProductFragment.products.addAll(shoppingList)
            this@ProductFragment.productAdapter.notifyDataSetChanged()
        }
    }

    private fun removeAllProducts() {
        mainScope.launch {
            withContext(Dispatchers.IO) {
                productRepository.deleteAllProducts()
            }
            getProductFromDatabase()
        }
    }
}
