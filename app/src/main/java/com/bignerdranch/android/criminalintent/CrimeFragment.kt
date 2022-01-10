package com.bignerdranch.android.criminalintent

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import java.util.*
import androidx.lifecycle.Observer

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = "0" // 申请拿到一个结果
private const val ARG_DATE = "date"

class CrimeFragment : Fragment() {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var actualResult: Date
    /**
     * 用库实现快速获取viewModel
     */
    private val crimeDetailViewModel : CrimeDetailViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * 监听实现，每个请求都需单独在onCreate中注册
         */
        setFragmentResultListener(REQUEST_DATE){
            requestKey, bundle ->
                actualResult = (bundle.get(ARG_DATE) as? Date)!!
                crime.date = actualResult
                updateUI()
            Log.d("saber","我们拿到了 $actualResult,检查对应请求key$requestKey")
        }


        crime = Crime() // 手动调用时 title是默认值
        val crimeId : UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        Log.d(ARG_CRIME_ID,"我们现在能拿到activity切换fragment时的crime id了，$crimeId")
        // 除了 crimeId，CrimeFragment中 crime的其他内容全都是默认值
        crimeDetailViewModel.loadCrime(crimeId) // 接下来我们可以观测crimeLiveData了
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox

        return view
    }

    /**
     *  观察数据变化，一有新数据发布就更新UI
     *
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 直接操作viewmodel即可
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer {
                crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            }
        )
    }

    override fun onStart() {5
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // TODO
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // TODO
            }
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        /**
         * 点击该按钮后，启动一个新的  fragment
         * 按照正常的fragment交互，假设我们在启动这个fragment时就是为了拿到某个回调结果。
         */
        dateButton.setOnClickListener{
            DatePickerFragment.newInstance(crime.date, REQUEST_DATE).apply {
//                this@CrimeFragment.parentFragmentManager.setFragmentResultListener()
                show(this@CrimeFragment.parentFragmentManager,DIALOG_DATE)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    /**
     * 定义一个ui刷新函数，用于观测到变动的liveData后进行UI刷新，否则UI就不动了！
     */
    private fun updateUI() {
        // titleField是可编辑对象，不能直接设置string，只能用setText
        titleField.setText(crime.title)
//        dateButton.text = crime.date.toString()
        dateButton.apply {
            text = DateFormat.format("EEE, MMM d, yyyy", crime.date)
        }
        solvedCheckBox.apply{
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState() // 跳过check动画
        }

    }

    companion object {
        fun newInstance(crimeId: UUID) : CrimeFragment{
            val args = Bundle().apply{ // 执行一个以参数传入的函数，该函数在当前对象上面执行，并返回当前对象
                // 创建bundle实例，写入内容
                putSerializable(ARG_CRIME_ID,crimeId)
            }

            return CrimeFragment().apply {
                arguments = args;
            }

        }
    }

}