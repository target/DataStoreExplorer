package com.target.datastorexplorer.ui

sealed class JsonTreeLoadingState {
	object Loading : JsonTreeLoadingState()
	object NotLoading : JsonTreeLoadingState()
}