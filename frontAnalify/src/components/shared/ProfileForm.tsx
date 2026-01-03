import React, { useState } from 'react';
import { User } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/hooks/use-toast';

export const ProfileForm: React.FC = () => {
  const { user, updateUser } = useAuth();
  const { toast } = useToast();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    userName: user?.userName || '',
    mail: user?.mail || '',
    dateOfBirth: user?.dateOfBirth || '',
  });
  const [hasChanges, setHasChanges] = useState(false);

  if (!user) return null;

  const handleChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setHasChanges(true);
  };

  const handleSave = async () => {
    try {
      // In production, this would call the API
      // await userApi.updateProfile(formData);
      updateUser(formData);
      setIsEditing(false);
      setHasChanges(false);
      toast({
        title: 'Profile Updated',
        description: 'Your profile has been updated successfully.',
      });
    } catch {
      toast({
        title: 'Error',
        description: 'Failed to update profile. Please try again.',
        variant: 'destructive',
      });
    }
  };

  const handleCancel = () => {
    setFormData({
      userName: user.userName,
      mail: user.mail,
      dateOfBirth: user.dateOfBirth,
    });
    setIsEditing(false);
    setHasChanges(false);
  };

  return (
    <div className="max-w-2xl">
      <div className="stat-card animate-fade-in">
        <div className="flex items-center gap-4 mb-6">
          <div className="p-4 bg-primary/10 rounded-full">
            <User className="h-8 w-8 text-primary" />
          </div>
          <div>
            <h2 className="text-xl font-semibold text-foreground">Personal Information</h2>
            <p className="text-sm text-muted-foreground">View and manage your profile details</p>
          </div>
        </div>

        <div className="grid gap-6">
          {/* Read-only fields */}
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label className="text-muted-foreground">User ID</Label>
              <Input value={user.userId} disabled className="bg-muted" />
            </div>
            <div className="space-y-2">
              <Label className="text-muted-foreground">Role</Label>
              <Input value={user.role} disabled className="bg-muted" />
            </div>
          </div>

          {/* Editable fields */}
          <div className="space-y-2">
            <Label htmlFor="userName">Username</Label>
            <Input
              id="userName"
              value={isEditing ? formData.userName : user.userName}
              onChange={(e) => handleChange('userName', e.target.value)}
              disabled={!isEditing}
              className={!isEditing ? 'bg-muted' : ''}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="mail">Email</Label>
            <Input
              id="mail"
              type="email"
              value={isEditing ? formData.mail : user.mail}
              onChange={(e) => handleChange('mail', e.target.value)}
              disabled={!isEditing}
              className={!isEditing ? 'bg-muted' : ''}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="dateOfBirth">Date of Birth</Label>
            <Input
              id="dateOfBirth"
              type="date"
              value={isEditing ? formData.dateOfBirth : user.dateOfBirth}
              onChange={(e) => handleChange('dateOfBirth', e.target.value)}
              disabled={!isEditing}
              className={!isEditing ? 'bg-muted' : ''}
            />
          </div>

          {/* Role-specific fields */}
          {user.storeId && (
            <div className="space-y-2">
              <Label className="text-muted-foreground">Store ID</Label>
              <Input value={user.storeId} disabled className="bg-muted" />
            </div>
          )}

          {user.salary !== undefined && user.salary > 0 && (
            <div className="space-y-2">
              <Label className="text-muted-foreground">Salary</Label>
              <Input
                value={`$${user.salary.toLocaleString()}`}
                disabled
                className="bg-muted"
              />
            </div>
          )}

          {user.dateStarted && (
            <div className="space-y-2">
              <Label className="text-muted-foreground">Date Started</Label>
              <Input value={user.dateStarted} disabled className="bg-muted" />
            </div>
          )}
        </div>

        <div className="mt-6 flex gap-3">
          {!isEditing ? (
            <Button onClick={() => setIsEditing(true)}>Edit Profile</Button>
          ) : (
            <>
              <Button onClick={handleSave} disabled={!hasChanges}>
                Save Changes
              </Button>
              <Button variant="outline" onClick={handleCancel}>
                Cancel
              </Button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
