using System;
using System.IO;
using System.Linq;
using NodeEditor.Editor.Scripts;
using UnityEditor;
using UnityEngine;

namespace NodeEditor.Scripts
{
	public class NoderGraphAssetPostProcessor : AssetPostprocessor
	{
		static void RegisterShaders(string[] paths)
		{
			foreach (var path in paths)
			{
				if (!path.EndsWith(NodeGraphImporter.ShaderGraphExtension, StringComparison.InvariantCultureIgnoreCase))
					continue;

				var mainObj = AssetDatabase.LoadMainAssetAtPath(path);
				if (mainObj is Shader)
					ShaderUtil.RegisterShader((Shader)mainObj);

				var objs = AssetDatabase.LoadAllAssetRepresentationsAtPath(path);
				foreach (var obj in objs)
				{
					if (obj is Shader)
						ShaderUtil.RegisterShader((Shader)obj);
				}
			}
		}

		static void UpdateAfterAssetChange(string[] newNames)
		{
			// This will change the title of the window.
			NodeGraphEditWindow[] windows = Resources.FindObjectsOfTypeAll<NodeGraphEditWindow>();
			foreach (var matGraphEditWindow in windows)
			{
				for (int i = 0; i < newNames.Length; ++i)
				{
					if (matGraphEditWindow.selectedGuid == AssetDatabase.AssetPathToGUID(newNames[i]))
						matGraphEditWindow.assetName = Path.GetFileNameWithoutExtension(newNames[i]).Split('/').Last();
				}
			}
		}

		static void OnPostprocessAllAssets(string[] importedAssets, string[] deletedAssets, string[] movedAssets, string[] movedFromAssetPaths)
		{
			RegisterShaders(importedAssets);

			bool anyShaders = movedAssets.Any(val => val.EndsWith(NodeGraphImporter.ShaderGraphExtension, StringComparison.InvariantCultureIgnoreCase));
			anyShaders |= movedAssets.Any(val => val.EndsWith("shadersubgraph", StringComparison.InvariantCultureIgnoreCase));
			if (anyShaders)
				UpdateAfterAssetChange(movedAssets);
		}
	}
}