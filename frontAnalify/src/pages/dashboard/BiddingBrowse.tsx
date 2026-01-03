import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Loader2, ChevronRight, TrendingUp } from 'lucide-react';
import { biddingApi } from '@/services/api';
import type { CategoryDTO } from '@/types';
import { useToast } from '@/hooks/use-toast';

export default function BiddingBrowse() {
  const [categories, setCategories] = useState<CategoryDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      setLoading(true);
      const data = await biddingApi.getCategories();
      setCategories(data as CategoryDTO[]);
    } catch (error) {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to load categories',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Browse Sections</h1>
          <p className="text-muted-foreground mt-1">
            Select a category to browse available sections for bidding
          </p>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {categories.map((category) => (
          <Card
            key={category.categoryId}
            className="hover:shadow-lg transition-shadow cursor-pointer"
            onClick={() => navigate(`/dashboard/bidding/category/${category.categoryId}`)}
          >
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>{category.categoryName}</CardTitle>
                <ChevronRight className="h-5 w-5 text-muted-foreground" />
              </div>
              <CardDescription>View available sections</CardDescription>
            </CardHeader>
            <CardContent>
              <Button className="w-full">
                <TrendingUp className="mr-2 h-4 w-4" />
                Browse Sections
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>

      {categories.length === 0 && (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-10">
            <p className="text-muted-foreground">No categories available for bidding</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
