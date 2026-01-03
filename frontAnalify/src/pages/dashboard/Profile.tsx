import React from 'react';
import { ProfileForm } from '@/components/shared/ProfileForm';

const Profile: React.FC = () => {
  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <h1 className="page-title">Personal Data</h1>
        <p className="page-description">View and update your personal information</p>
      </div>
      
      <ProfileForm />
    </div>
  );
};

export default Profile;
