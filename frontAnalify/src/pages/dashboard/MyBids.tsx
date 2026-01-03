import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Loader2, TrendingUp, Trophy, X, Package } from 'lucide-react';
import { biddingApi } from '@/services/api';
import type { BidDTO, SectionDTO } from '@/types';
import { useToast } from '@/hooks/use-toast';

export default function MyBids() {
  const [allBids, setAllBids] = useState<BidDTO[]>([]);
  const [currentWinningBids, setCurrentWinningBids] = useState<BidDTO[]>([]);
  const [finalWinningBids, setFinalWinningBids] = useState<BidDTO[]>([]);
  const [possessedSections, setPossessedSections] = useState<SectionDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState<number | null>(null);
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    loadBids();
  }, []);

  const loadBids = async () => {
    try {
      setLoading(true);
      const [all, currentWinning, finalWinning, possessed] = await Promise.all([
        biddingApi.getMyBids(),
        biddingApi.getMyCurrentWinningBids(),
        biddingApi.getMyWinningBids(),
        biddingApi.getMyPossessions(),
      ]);
      setAllBids(all as BidDTO[]);
      setCurrentWinningBids(currentWinning as BidDTO[]);
      setFinalWinningBids(finalWinning as BidDTO[]);
      setPossessedSections(possessed as SectionDTO[]);
    } catch (error) {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to load bids',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCancelBid = async (bidId: number) => {
    try {
      setCancelling(bidId);
      await biddingApi.cancelBid(bidId);
      toast({
        title: 'Bid Cancelled',
        description: 'Your bid has been cancelled successfully',
      });
      await loadBids();
    } catch (error) {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to cancel bid',
        variant: 'destructive',
      });
    } finally {
      setCancelling(null);
    }
  };

  const getBidStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <Badge variant="default">Winning</Badge>;
      case 'OUTBID':
        return <Badge variant="secondary">Outbid</Badge>;
      case 'WINNER':
        return <Badge className="bg-green-600">Winner</Badge>;
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };

  const renderBidCard = (bid: BidDTO, showCancel: boolean = false) => (
    <Card key={bid.bidId} className="hover:shadow-lg transition-shadow">
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-lg">{bid.sectionName}</CardTitle>
          {getBidStatusBadge(bid.status)}
        </div>
        <CardDescription>
          Placed on {new Date(bid.bidTime).toLocaleString()}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          <div className="flex justify-between items-center">
            <span className="text-muted-foreground">Bid Amount:</span>
            <span className="text-2xl font-bold text-primary">{bid.amount.toFixed(2)} DH</span>
          </div>
          
          <div className="flex gap-2">
            <Button
              variant="outline"
              className="flex-1"
              onClick={() => navigate(`/dashboard/bidding/section/${bid.sectionId}`)}
            >
              View Section
            </Button>
            
            {showCancel && bid.status === 'OUTBID' && (
              <Button
                variant="destructive"
                onClick={() => handleCancelBid(bid.bidId)}
                disabled={cancelling === bid.bidId}
              >
                {cancelling === bid.bidId ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <X className="h-4 w-4" />
                )}
              </Button>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );

  const renderSectionCard = (section: SectionDTO) => (
    <Card key={section.sectionId} className="hover:shadow-lg transition-shadow">
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-lg">{section.sectionName}</CardTitle>
          <Badge className="bg-green-600">Possessed</Badge>
        </div>
        <CardDescription>
          {section.description || 'Monthly contract section'}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <span className="text-sm text-muted-foreground">Base Price:</span>
              <p className="font-semibold">{section.basePrice.toFixed(2)} DH</p>
            </div>
            <div>
              <span className="text-sm text-muted-foreground">Won At:</span>
              <p className="font-semibold">{section.currentPrice.toFixed(2)} DH</p>
            </div>
          </div>
          
          <div className="space-y-1">
            <span className="text-sm text-muted-foreground">Status:</span>
            <p className="font-medium">{section.status}</p>
          </div>
          
          <Button
            variant="outline"
            className="w-full"
            onClick={() => navigate(`/dashboard/bidding/section/${section.sectionId}`)}
          >
            View Details
          </Button>
        </div>
      </CardContent>
    </Card>
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">My Bids</h1>
        <p className="text-muted-foreground mt-1">
          View and manage all your bids
        </p>
      </div>

      <Tabs defaultValue="all" className="space-y-4">
        <TabsList>
          <TabsTrigger value="all">
            <TrendingUp className="mr-2 h-4 w-4" />
            All Bids ({allBids.length})
          </TabsTrigger>
          <TabsTrigger value="current-winning">
            <TrendingUp className="mr-2 h-4 w-4" />
            Currently Winning ({currentWinningBids.length})
          </TabsTrigger>
          <TabsTrigger value="final-winning">
            <Trophy className="mr-2 h-4 w-4" />
            Final Wins ({finalWinningBids.length})
          </TabsTrigger>
          <TabsTrigger value="possessions">
            <Package className="mr-2 h-4 w-4" />
            My Possessions ({possessedSections.length})
          </TabsTrigger>
        </TabsList>

        <TabsContent value="all" className="space-y-4">
          {allBids.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-10">
                <p className="text-muted-foreground mb-4">You haven't placed any bids yet</p>
                <Button onClick={() => navigate('/dashboard/bidding')}>
                  <TrendingUp className="mr-2 h-4 w-4" />
                  Browse Sections
                </Button>
              </CardContent>
            </Card>
          ) : (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {allBids.map((bid) => renderBidCard(bid, true))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="current-winning" className="space-y-4">
          {currentWinningBids.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-10">
                <TrendingUp className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground mb-4">You don't have any currently winning bids</p>
                <Button onClick={() => navigate('/dashboard/bidding')}>
                  <TrendingUp className="mr-2 h-4 w-4" />
                  Browse Sections
                </Button>
              </CardContent>
            </Card>
          ) : (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {currentWinningBids.map((bid) => renderBidCard(bid, false))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="final-winning" className="space-y-4">
          {finalWinningBids.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-10">
                <Trophy className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground mb-4">You haven't won any sections yet</p>
                <Button onClick={() => navigate('/dashboard/bidding')}>
                  <TrendingUp className="mr-2 h-4 w-4" />
                  Browse Sections
                </Button>
              </CardContent>
            </Card>
          ) : (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {finalWinningBids.map((bid) => renderBidCard(bid, false))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="possessions" className="space-y-4">
          {possessedSections.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-10">
                <Package className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground mb-4">You don't currently possess any sections</p>
                <p className="text-sm text-muted-foreground mb-4">Win bids to acquire sections for monthly contracts</p>
                <Button onClick={() => navigate('/dashboard/bidding')}>
                  <TrendingUp className="mr-2 h-4 w-4" />
                  Browse Sections
                </Button>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              <div className="bg-green-50 dark:bg-green-950 border border-green-200 dark:border-green-800 rounded-lg p-4">
                <h3 className="font-semibold text-green-900 dark:text-green-100 mb-2">
                  You are currently possessing {possessedSections.length} section{possessedSections.length !== 1 ? 's' : ''}
                </h3>
                <p className="text-sm text-green-700 dark:text-green-300">
                  These sections were won through bidding and are now under your contract
                </p>
              </div>
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {possessedSections.map((section) => renderSectionCard(section))}
              </div>
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
