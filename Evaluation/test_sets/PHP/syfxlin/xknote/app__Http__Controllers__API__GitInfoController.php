<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Http\Models\GitInfoModel;

class GitInfoController extends Controller
{
    public function getConfig(Request $request)
    {
        $id = $request->user()->id;
        $git_info = GitInfoModel::where('uid', $id);
        if ($git_info->count() <= 0) {
            return response(
                [
                    'error' => true,
                    'message' => 'You need to set up Git information'
                ],
                404
            );
        }
        $re_info = $git_info->get()[0];
        unset($re_info->git_password);
        return ['error' => false, 'config' => $re_info];
    }

    public function setConfig(Request $request)
    {
        $id = $request->user()->id;
        if (
            !$request->has('git_password') ||
            !$request->has('git_name') ||
            !$request->has('git_email')
        ) {
            return response(
                [
                    'error' =>
                        'Parameter not found. (git_password,git_name,git_email)'
                ],
                400
            );
        }
        $data = [
            'git_name' => $request->git_name,
            'git_email' => $request->git_email,
            'git_password' => encrypt($request->git_password)
        ];
        $info_m = GitInfoModel::where('uid', $id);
        if ($info_m->count() <= 0) {
            $data['uid'] = $id;
            GitInfoModel::create($data);
        } else {
            $info_m->update($data);
        }
        return ['error' => false];
    }
}
