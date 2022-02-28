using UnityEngine;



namespace BuildReportTool.Window.Screen
{

public class SizeStats : BaseScreen
{
	public override string Name { get{ return Labels.SIZE_STATS_CATEGORY_LABEL; } }

	public override void RefreshData(BuildInfo buildReport)
	{
	}

	Vector2 _assetListScrollPos;

	public override void DrawGUI(Rect position, BuildInfo buildReportToDisplay)
	{
		GUILayout.Space(2); // top padding for scrollbar

		_assetListScrollPos = GUILayout.BeginScrollView(_assetListScrollPos);

		GUILayout.Space(10); // top padding for content

		GUILayout.BeginHorizontal();
			GUILayout.Space(10); // extra left padding

			DrawTotalSize(buildReportToDisplay);

			GUILayout.Space(BuildReportTool.Window.Settings.CATEGORY_HORIZONTAL_SPACING);
			GUILayout.BeginVertical();

				DrawBuildSizes(buildReportToDisplay);

				GUILayout.Space(BuildReportTool.Window.Settings.CATEGORY_VERTICAL_SPACING);

				DrawDLLList(buildReportToDisplay);

			GUILayout.EndVertical();
			GUILayout.Space(20); // extra right padding
		GUILayout.EndHorizontal();

		GUILayout.EndScrollView();
	}


	void DrawTotalSize(BuildInfo buildReportToDisplay)
	{
		GUILayout.BeginVertical();


		if (buildReportToDisplay.HasOldSizeValues)
		{
			// in old sizes:
			// TotalBuildSize is really the used assets size
			// CompressedBuildSize if present is the total build size

			BuildReportTool.Window.Utility.DrawLargeSizeDisplay(Labels.USED_TOTAL_SIZE_LABEL, Labels.USED_TOTAL_SIZE_DESC, buildReportToDisplay.TotalBuildSize);
			GUILayout.Space(40);
			BuildReportTool.Window.Utility.DrawLargeSizeDisplay(Labels.BUILD_TOTAL_SIZE_LABEL, BuildReportTool.Window.Utility.GetProperBuildSizeDesc(buildReportToDisplay), buildReportToDisplay.CompressedBuildSize);
		}
		else
		{
			// Total Build Size
			if (!string.IsNullOrEmpty(buildReportToDisplay.TotalBuildSize) && !string.IsNullOrEmpty(buildReportToDisplay.BuildFilePath))
			{
				GUILayout.BeginVertical();
					GUILayout.Label(Labels.BUILD_TOTAL_SIZE_LABEL, BuildReportTool.Window.Settings.INFO_TITLE_STYLE_NAME);
					
					GUILayout.Label(BuildReportTool.Util.GetBuildSizePathDescription(buildReportToDisplay), BuildReportTool.Window.Settings.TINY_HELP_STYLE_NAME);

					GUILayout.Label(buildReportToDisplay.TotalBuildSize, BuildReportTool.Window.Settings.BIG_NUMBER_STYLE_NAME);
				GUILayout.EndVertical();

				DrawAuxiliaryBuildSizes(buildReportToDisplay);
				GUILayout.Space(40);
			}


			// Used Assets
			if (!string.IsNullOrEmpty(buildReportToDisplay.UsedTotalSize))
			{
				BuildReportTool.Window.Utility.DrawLargeSizeDisplay(Labels.USED_TOTAL_SIZE_LABEL, Labels.USED_TOTAL_SIZE_DESC, buildReportToDisplay.UsedTotalSize);
				GUILayout.Space(40);
			}


			// Unused Assets
			if (buildReportToDisplay.UnusedAssetsIncludedInCreation)
			{
				BuildReportTool.Window.Utility.DrawLargeSizeDisplay(Labels.UNUSED_TOTAL_SIZE_LABEL, Labels.UNUSED_TOTAL_SIZE_DESC, buildReportToDisplay.UnusedTotalSize);
			}
		}

		GUILayout.EndVertical();
	}




	void DrawAuxiliaryBuildSizes(BuildInfo buildReportToDisplay)
	{
		BuildReportTool.BuildPlatform buildPlatform = BuildReportTool.ReportGenerator.GetBuildPlatformFromString(buildReportToDisplay.BuildType, buildReportToDisplay.BuildTargetUsed);

		if (buildPlatform == BuildReportTool.BuildPlatform.Web)
		{
			GUILayout.Space(20);
			GUILayout.BeginVertical();
				GUILayout.Label(Labels.WEB_UNITY3D_FILE_SIZE_LABEL, BuildReportTool.Window.Settings.INFO_SUBTITLE_BOLD_STYLE_NAME);
				GUILayout.Label(buildReportToDisplay.WebFileBuildSize, BuildReportTool.Window.Settings.BIG_NUMBER_STYLE_NAME);
			GUILayout.EndVertical();
		}
		else if (buildPlatform == BuildReportTool.BuildPlatform.Android)
		{
			if (!buildReportToDisplay.AndroidCreateProject && buildReportToDisplay.AndroidUseAPKExpansionFiles)
			{
				GUILayout.Space(20);
				GUILayout.BeginVertical();
					GUILayout.Label(Labels.ANDROID_APK_FILE_SIZE_LABEL, BuildReportTool.Window.Settings.INFO_SUBTITLE_BOLD_STYLE_NAME);
					GUILayout.Label(buildReportToDisplay.AndroidApkFileBuildSize, BuildReportTool.Window.Settings.INFO_TITLE_STYLE_NAME);
				GUILayout.EndVertical();

				GUILayout.Space(20);
				GUILayout.BeginVertical();
					GUILayout.Label(Labels.ANDROID_OBB_FILE_SIZE_LABEL, BuildReportTool.Window.Settings.INFO_SUBTITLE_BOLD_STYLE_NAME);
					GUILayout.Label(buildReportToDisplay.AndroidObbFileBuildSize, BuildReportTool.Window.Settings.INFO_TITLE_STYLE_NAME);
				GUILayout.EndVertical();
			}
			else if (buildReportToDisplay.AndroidCreateProject && buildReportToDisplay.AndroidUseAPKExpansionFiles)
			{
				GUILayout.Space(20);
				GUILayout.BeginVertical();
					GUILayout.Label(Labels.ANDROID_OBB_FILE_SIZE_LABEL, BuildReportTool.Window.Settings.INFO_SUBTITLE_BOLD_STYLE_NAME);
					GUILayout.Label(buildReportToDisplay.AndroidObbFileBuildSize, BuildReportTool.Window.Settings.INFO_TITLE_STYLE_NAME);
				GUILayout.EndVertical();
			}
		}

		// Streaming Assets
		if (buildReportToDisplay.HasStreamingAssets)
		{
			GUILayout.Space(20);
			BuildReportTool.Window.Utility.DrawLargeSizeDisplay(Labels.STREAMING_ASSETS_TOTAL_SIZE_LABEL, Labels.STREAMING_ASSETS_SIZE_DESC, buildReportToDisplay.StreamingAssetsSize);
		}
	}










	void DrawBuildSizes(BuildInfo buildReportToDisplay)
	{
		if (!string.IsNullOrEmpty(buildReportToDisplay.CompressedBuildSize))
		{
			GUILayout.BeginVertical();
		}

		GUILayout.Label(Labels.TOTAL_SIZE_BREAKDOWN_LABEL, BuildReportTool.Window.Settings.INFO_TITLE_STYLE_NAME);

		if (!string.IsNullOrEmpty(buildReportToDisplay.CompressedBuildSize))
		{
			GUILayout.BeginHorizontal();
				GUILayout.Label(Labels.TOTAL_SIZE_BREAKDOWN_MSG_PRE_BOLD, BuildReportTool.Window.Settings.INFO_SUBTITLE_STYLE_NAME);
				GUILayout.Label(Labels.TOTAL_SIZE_BREAKDOWN_MSG_BOLD, BuildReportTool.Window.Settings.INFO_SUBTITLE_BOLD_STYLE_NAME);
				GUILayout.Label(Labels.TOTAL_SIZE_BREAKDOWN_MSG_POST_BOLD, BuildReportTool.Window.Settings.INFO_SUBTITLE_STYLE_NAME);
				GUILayout.FlexibleSpace();
			GUILayout.EndHorizontal();

			GUILayout.EndVertical();
		}

		if (buildReportToDisplay.BuildSizes != null)
		{
			GUILayout.BeginHorizontal(GUILayout.MaxWidth(500));

			DrawNames(buildReportToDisplay.BuildSizes);
			DrawReadableSizes(buildReportToDisplay.BuildSizes);
			DrawPercentages(buildReportToDisplay.BuildSizes);

			GUILayout.EndHorizontal();
		}
	}


	void DrawDLLList(BuildInfo buildReportToDisplay)
	{
		BuildReportTool.BuildPlatform buildPlatform = BuildReportTool.ReportGenerator.GetBuildPlatformFromString(buildReportToDisplay.BuildType, buildReportToDisplay.BuildTargetUsed);

		GUILayout.BeginHorizontal();

			GUILayout.BeginVertical();
				GUILayout.Label(Labels.MONO_DLLS_LABEL, BuildReportTool.Window.Settings.INFO_TITLE_STYLE_NAME);
				{
					GUILayout.BeginHorizontal(GUILayout.MaxWidth(500));
						DrawNames(buildReportToDisplay.MonoDLLs);
						DrawReadableSizes(buildReportToDisplay.MonoDLLs);
					GUILayout.EndHorizontal();
				}
			GUILayout.EndVertical();

			GUILayout.Space(15);

			GUILayout.BeginVertical();
				GUILayout.Label(Labels.SCRIPT_DLLS_LABEL, BuildReportTool.Window.Settings.INFO_TITLE_STYLE_NAME);
				{
					GUILayout.BeginHorizontal(GUILayout.MaxWidth(500));
						DrawNames(buildReportToDisplay.ScriptDLLs);

						if (buildPlatform != BuildPlatform.WebGL)
						{
							DrawReadableSizes(buildReportToDisplay.ScriptDLLs);
						}
					GUILayout.EndHorizontal();
				}
			GUILayout.EndVertical();

		GUILayout.EndHorizontal();
	}


	void DrawNames(BuildReportTool.SizePart[] list)
	{
		GUILayout.BeginVertical();
		bool useAlt = false;
		foreach (BuildReportTool.SizePart b in list)
		{
			if (b.IsTotal) continue;
			string styleToUse = useAlt ? BuildReportTool.Window.Settings.LIST_NORMAL_ALT_STYLE_NAME : BuildReportTool.Window.Settings.LIST_NORMAL_STYLE_NAME;
			GUILayout.Label(b.Name, styleToUse);
			useAlt = !useAlt;
		}
		GUILayout.EndVertical();
	}
	void DrawReadableSizes(BuildReportTool.SizePart[] list)
	{
		GUILayout.BeginVertical();
		bool useAlt = false;
		foreach (BuildReportTool.SizePart b in list)
		{
			if (b.IsTotal) continue;
			string styleToUse = useAlt ? BuildReportTool.Window.Settings.LIST_NORMAL_ALT_STYLE_NAME : BuildReportTool.Window.Settings.LIST_NORMAL_STYLE_NAME;
			GUILayout.Label(b.Size, styleToUse);
			useAlt = !useAlt;
		}
		GUILayout.EndVertical();
	}
	void DrawPercentages(BuildReportTool.SizePart[] list)
	{
		GUILayout.BeginVertical();
		bool useAlt = false;
		foreach (BuildReportTool.SizePart b in list)
		{
			if (b.IsTotal) continue;
			string styleToUse = useAlt ? BuildReportTool.Window.Settings.LIST_NORMAL_ALT_STYLE_NAME : BuildReportTool.Window.Settings.LIST_NORMAL_STYLE_NAME;
			GUILayout.Label(b.Percentage + "%", styleToUse);
			useAlt = !useAlt;
		}
		GUILayout.EndVertical();
	}
}

}
