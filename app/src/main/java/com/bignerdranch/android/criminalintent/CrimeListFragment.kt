package com.bignerdranch.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {

    /**
     * Required interface for hosting activities
     */
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null


    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList()) // 临时存储，用于给crimeRecyclerView的adapter属性赋值。livedata后需要非null默认值
    private val crimeListViewModel: CrimeListViewModel by viewModels()
//    private val crimeListViewModel: CrimeListViewModel by lazy {
//        Log.d(TAG, "saber_hello")
//        ViewModelProvider(this).get(CrimeListViewModel::class.java)
//    }


//  由于改为LiveData观测，日志不再有意义
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.d(TAG, "saber_Total crimes: ${crimeListViewModel.crimes.size}")
//    }

    companion object {
        fun newInstance(): CrimeListFragment {
            Log.d(TAG, "saber_hello")
            return CrimeListFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context) // 这一步含义是？
        crimeRecyclerView.adapter = adapter; // liveData观测后新增，因adapter不再为空
//        livedata观测化后，不再有意义
        // RecyclerView绑定布局文件并完成布局服务后，注入adapter和ViewHolder
//        updateUI()

        // 最终还是返回 inflate生成的布局文件
        // 中间对 RecyclerView做id绑定，并设置布局管理器
        return view
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 默认return false，故需要自己重写
        return when(item.itemId) {
            R.id.new_crime -> {
                val crime = Crime() // new一个新的，然后进行fragment切换
                // 添加到VM中，刷新到数据库
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimesListLiveData.observe(
            viewLifecycleOwner,
            Observer { crimes ->
                crimes?.let {
                    Log.i(TAG,"Got crimes ${crimes.size}")
                    updateUI(crimes) // 此处拿到数据后已经刷新了adapter
                }

            }
        )
    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        // Model 这边负责获取数据
        // 如何保证同名ID能识别到？

        private lateinit var crime: Crime
        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = this.crime.date.toString()
            solvedImageView.visibility = if(crime.isSolved){
                View.VISIBLE
            }else{
                View.GONE
            }
        }

        override fun onClick(v: View) {
//            Toast.makeText(context, "${crime.title} pressed", Toast.LENGTH_SHORT).show()
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    // 输入一个Crime List，作为变量去生成整个Recycler View所需的数据
    private inner class CrimeAdapter(var crimes: List<Crime>) :
        RecyclerView.Adapter<CrimeHolder>() {

        // 负责创建每一个单独的列表项显示的视图
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        // 负责将数据集中指定位置的crime数据发送给指定的ViewHolder，具体, m的ViewHolder内部管理我们不可见。Adapter已经获取了整体crimes
        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]

            holder.bind(crime)
        }

        override fun getItemCount() = crimes.size
    }

    private fun updateUI(crimes:List<Crime>) {
//        改为观测后，不再直接引用
//        val crimes = crimeListViewModel.crimes
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }
}