package ru.netology.nmedia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import ru.netology.nmedia.fragments.FeedFragment.Companion.textArgs
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.util.AndroidUtils
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

        val photoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.image_pick_error), Toast.LENGTH_SHORT
                ).show()
                return@registerForActivityResult
            }
            val uri = result.data?.data ?: return@registerForActivityResult

            viewModel.updatePhoto(uri, uri.toFile())
        }



        arguments?.textArgs?.let(binding.edit::setText)
        val startContent = binding.edit.text.toString()
        if (startContent == "" && viewModel.gDraftContent != "") {
            binding.edit.setText(viewModel.gDraftContent)
        }




        binding.edit.requestFocus()




        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(
                menu: Menu,
                menuInflater: MenuInflater
            ) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
//                        fragmentBinding?.let {
                        viewModel.changeContent(binding.edit.text.toString())
                        viewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
//                        }
                        true
                    }

                    else -> false
                }

        }, viewLifecycleOwner
        )


//        binding.ok.setOnClickListener {
//            if (binding.edit.text.isNotBlank()) {
//                val content = binding.edit.text.toString()
//                viewModel.changeContent(content)
//                viewModel.save()
//            }
//            viewModel.gDraftContent = ""
//            findNavController().navigateUp()
//        }

        viewModel.photo.observe(viewLifecycleOwner){photo ->
            if (photo == null){
                binding.photoContainer.isGone = true
                return@observe
            }
            binding.preview.setImageURI(photo.uri)
            binding.photoContainer.isVisible = true
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .createIntent(photoLauncher::launch)
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .createIntent(photoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            // Здесь вызывается метод ViewModel, который очищает URI и File
            viewModel.updatePhoto(null, null)
        }




        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(context, "Bye", Toast.LENGTH_LONG).show()
                if (startContent == "") {
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
            viewModel.clearPhoto()
            findNavController().navigateUp()
        }

        return binding.root

    }
}