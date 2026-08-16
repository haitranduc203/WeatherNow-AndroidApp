package com.example.weathernow.presentation

import com.example.weathernow.presentation.home.HomeViewModel
import com.example.weathernow.presentation.search.SearchViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BaselineViewModelTest {

    @Test
    fun homeViewModel_initialState_isNotLoading() = runTest {
        val viewModel = HomeViewModel()
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals("Select Location", state.locationName)
    }

    @Test
    fun searchViewModel_queryUpdate_updatesState() = runTest {
        val viewModel = SearchViewModel()
        viewModel.onQueryChanged("Hanoi")
        val state = viewModel.uiState.first()
        assertEquals("Hanoi", state.query)
    }
}
