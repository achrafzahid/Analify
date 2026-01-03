import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Loader2, ChevronLeft, ChevronRight } from 'lucide-react';
import { biddingApi } from '@/services/api';
import type { RangDTO, FaceDTO, SectionDTO } from '@/types';
import { useToast } from '@/hooks/use-toast';

type ViewMode = 'rangs' | 'faces' | 'sections';

export default function BiddingCategory() {
  const { categoryId } = useParams<{ categoryId: string }>();
  const [rangs, setRangs] = useState<RangDTO[]>([]);
  const [faces, setFaces] = useState<FaceDTO[]>([]);
  const [sections, setSections] = useState<SectionDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState<ViewMode>('rangs');
  const [selectedRangId, setSelectedRangId] = useState<number | null>(null);
  const [selectedFaceId, setSelectedFaceId] = useState<number | null>(null);
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    if (categoryId) {
      loadRangs(Number(categoryId));
    }
  }, [categoryId]);

  const loadRangs = async (catId: number) => {
    try {
      setLoading(true);
      const data = await biddingApi.getRangsByCategory(catId);
      setRangs(data as RangDTO[]);
      setViewMode('rangs');
    } catch (error) {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to load rangs',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  const loadFaces = async (rangId: number) => {
    try {
      setLoading(true);
      setSelectedRangId(rangId);
      const data = await biddingApi.getFacesByRang(rangId);
      setFaces(data as FaceDTO[]);
      setViewMode('faces');
    } catch (error) {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to load faces',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  const loadSections = async (faceId: number) => {
    try {
      setLoading(true);
      setSelectedFaceId(faceId);
      const data = await biddingApi.getSectionsByFace(faceId);
      setSections(data as SectionDTO[]);
      setViewMode('sections');
    } catch (error) {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to load sections',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleBack = () => {
    if (viewMode === 'faces') {
      setViewMode('rangs');
    } else if (viewMode === 'sections') {
      setViewMode('faces');
    } else {
      navigate('/dashboard/bidding');
    }
  };

  const getStatusBadge = (status: string) => {
    if (status === 'CLOSED') {
      return <Badge variant="secondary">Closed</Badge>;
    }
    if (status.startsWith('OPEN-BIDDEN')) {
      return <Badge variant="default">Active Bidding</Badge>;
    }
    return <Badge variant="outline">Open</Badge>;
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
      <div className="flex items-center gap-4">
        <Button variant="outline" onClick={handleBack}>
          <ChevronLeft className="mr-2 h-4 w-4" />
          Back
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            {viewMode === 'rangs' && 'Select Rang'}
            {viewMode === 'faces' && 'Select Face'}
            {viewMode === 'sections' && 'Available Sections'}
          </h1>
          <p className="text-muted-foreground mt-1">
            {viewMode === 'rangs' && 'Choose a rang to view faces'}
            {viewMode === 'faces' && 'Choose a face to view sections'}
            {viewMode === 'sections' && 'Select a section to place your bid'}
          </p>
        </div>
      </div>

      {/* Rangs View */}
      {viewMode === 'rangs' && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {rangs.map((rang) => (
            <Card
              key={rang.rangId}
              className="hover:shadow-lg transition-shadow cursor-pointer"
              onClick={() => loadFaces(rang.rangId)}
            >
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle>{rang.rangName}</CardTitle>
                  <ChevronRight className="h-5 w-5 text-muted-foreground" />
                </div>
                {rang.description && (
                  <CardDescription>{rang.description}</CardDescription>
                )}
              </CardHeader>
            </Card>
          ))}
        </div>
      )}

      {/* Faces View */}
      {viewMode === 'faces' && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {faces.map((face) => (
            <Card
              key={face.faceId}
              className="hover:shadow-lg transition-shadow cursor-pointer"
              onClick={() => loadSections(face.faceId)}
            >
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle>{face.faceName}</CardTitle>
                  <ChevronRight className="h-5 w-5 text-muted-foreground" />
                </div>
                {face.description && (
                  <CardDescription>{face.description}</CardDescription>
                )}
              </CardHeader>
            </Card>
          ))}
        </div>
      )}

      {/* Sections View */}
      {viewMode === 'sections' && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {sections.map((section) => (
            <Card
              key={section.sectionId}
              className="hover:shadow-lg transition-shadow cursor-pointer"
              onClick={() => navigate(`/dashboard/bidding/section/${section.sectionId}`)}
            >
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle>{section.sectionName}</CardTitle>
                  {getStatusBadge(section.status)}
                </div>
                {section.description && (
                  <CardDescription>{section.description}</CardDescription>
                )}
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Base Price:</span>
                    <span className="font-medium">{section.basePrice.toFixed(2)} DH</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Current Price:</span>
                    <span className="font-bold text-primary">{section.currentPrice.toFixed(2)} DH</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Deadline:</span>
                    <span>{new Date(section.dateDelai).toLocaleDateString()}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
