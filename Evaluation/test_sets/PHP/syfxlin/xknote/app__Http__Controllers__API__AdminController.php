<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Http\Models\ConfigModel;
use App\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Gate;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use App\Http\Models\NoteModel;
use App\Http\Models\FolderModel;

class AdminController extends Controller
{
    public function __construct()
    {
    }

    public function createUser(Request $request)
    {
        $data = [
            'username' => $request->username,
            'nickname' => $request->nickname,
            'email' => $request->email,
            'password' => $request->password,
            'password_confirmation' => $request->password_confirmation
        ];
        $right = $this->validator($data)->passes();
        if (!$right) {
            return response(
                [
                    'error' => 'Parameter error.'
                ],
                400
            );
        }
        return ['error' => false, 'user' => $this->create($data)];
    }

    public function validator($data)
    {
        return Validator::make($data, [
            'username' => ['required', 'string', 'max:255'],
            'nickname' => ['required', 'string', 'max:255'],
            'email' => [
                'required',
                'string',
                'email',
                'max:255',
                'unique:users'
            ],
            'password' => ['required', 'string', 'min:8', 'confirmed']
        ]);
    }

    public function create($data)
    {
        return User::create([
            'username' => $data['username'],
            'nickname' => $data['nickname'],
            'email' => $data['email'],
            'password' => Hash::make($data['password'])
        ]);
    }

    public function getUser(Request $request)
    {
        return ['error' => false, 'users' => User::all()];
    }

    public function deleteUser(Request $request, $id)
    {
        if ($id == 1) {
            return response(
                [
                    'error' =>
                        'You are an administrator and cannot delete yourself.'
                ],
                409
            );
        }
        $user = User::find($id);
        if (!$user) {
            return response(
                [
                    'error' => 'User not found.'
                ],
                404
            );
        }
        $user->delete();
        return ['error' => false];
    }

    public function editUser(Request $request, $id)
    {
        $user = User::find($id);
        $data = [
            'username' => $request->username,
            'nickname' => $request->nickname,
            'email' => $request->email,
            'password' => $request->password,
            'password_confirmation' => $request->password_confirmation
        ];
        $right = $this->validator($data)->passes();
        if (!$right) {
            return response(
                [
                    'error' => 'Parameter error.'
                ],
                400
            );
        }
        $user->update($data);
        return [
            'error' => false,
            'user' => User::find($id)
        ];
    }

    public function getUserNoteCount(Request $request, $id)
    {
        $note_m = new NoteModel();
        $notes = $note_m->getAll('uid_' . $id);
        return [
            'error' => false,
            'count' => count($notes)
        ];
    }

    public function deleteUserNote(Request $request, $id)
    {
        $folder_m = new FolderModel();
        $folder_m->delete('uid_' . $id);
        return ['error' => false];
    }

    public function getConfig(Request $request)
    {
        $config_m = ConfigModel::get();
        $config = [];
        foreach ($config_m as $value) {
            $config[$value->config_name] = $value->config_value;
        }
        $config['enable_register'] = (bool) $config['enable_register'];
        return ['error' => false, 'config' => $config];
    }

    public function setConfig(Request $request)
    {
        $enable_register = $request->enable_register;
        $xknote_name = $request->xknote_name;
        $upload_limit = $request->upload_limit;
        ConfigModel::where('config_name', 'enable_register')->update([
            'config_value' => $enable_register
        ]);
        ConfigModel::where('config_name', 'xknote_name')->update([
            'config_value' => $xknote_name
        ]);
        ConfigModel::where('config_name', 'upload_limit')->update([
            'config_value' => $upload_limit
        ]);
        return ['error' => false];
    }
}
