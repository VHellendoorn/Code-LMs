<?php

namespace App\Http\Livewire\Admin\Dashboard;

use App\Models\Appointment;
use Livewire\Component;

class AppointmentsCount extends Component
{
    public $appointmentsCount;

    public function mount()
    {
        $this->getAppointmentsCount();
    }

    public function getAppointmentsCount($status = null)
    {
        $this->appointmentsCount = Appointment::query()
            ->when($status, function ($query, $status) {
                return $query->where('status', $status);
            })
            ->count();
    }

    public function render()
    {
        return view('livewire.admin.dashboard.appointments-count');
    }
}
