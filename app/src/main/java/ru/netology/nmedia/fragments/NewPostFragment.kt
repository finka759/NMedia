package ru.netology.nmedia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.fragments.FeedFragment.Companion.textArgs
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel


class NewPostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false,
        )

        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        arguments?.textArgs?.let(binding.edit::setText)
        val startContent = binding.edit.text.toString()
        if (startContent == "" && viewModel.gDraftContent != "") {
            binding.edit.setText(viewModel.gDraftContent)
        }

        binding.edit.requestFocus()

        binding.ok.setOnClickListener {
            if (binding.edit.text.isNotBlank()) {
                val content = binding.edit.text.toString()
                viewModel.changeContent(content)
                viewModel.save()
            }
            viewModel.gDraftContent = ""
            findNavController().navigateUp()
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Toast.makeText(context, "Bye", Toast.LENGTH_LONG).show()
                if (startContent == ""){
                    viewModel.gDraftContent = binding.edit.text.toString()
                }
                viewModel.setEmtyPostToEdited()
                findNavController().navigateUp()
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        return binding.root

    }
}