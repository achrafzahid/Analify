import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Loader2, TrendingUp, Trophy, Calendar, DollarSign, Clock, Info, Package } from 'lucide-react';
import { biddingApi } from '@/services/api';
import type { SeasonConfigDTO, BidDTO, SectionDTO } from '@/types';
import { useToast } from '@/hooks/use-toast';

export default function BiddingDashboard() {
  const [seasonInfo, setSeasonInfo] = useState<SeasonConfigDTO | null>(null);
  const [myBids, setMyBids] = useState<BidDTO[]>([]);
  const [currentWinningBids, setCurrentWinningBids] = useState<BidDTO[]>([]);
  const [possessedSections, setPossessedSections] = useState<SectionDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [season, bids, winning, possessed] = await Promise.all([
        biddingApi.getSeasonInfo(),
        biddingApi.getMyBids(),
        biddingApi.getMyCurrentWinningBids(),
        biddingApi.getMyPossessions(),
      ]);

      setSeasonInfo(season as SeasonConfigDTO);
      setMyBids(bids as BidDTO[]);
      setCurrentWinningBids(winning as BidDTO[]);
      setPossessedSections(possessed as SectionDTO[]);
    } catch (error) {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to load dashboard data',
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

  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Bidding Dashboard</h1>
        <p className="text-muted-foreground mt-1">
          Overview of current bidding period and your bids
        </p>
      </div>

      {/* Season/Period Information */}
      {seasonInfo && (
        <Alert className={seasonInfo.isBiddingOpen ? 'border-green-500' : 'border-yellow-500'}>
          <Info className="h-4 w-4" />
          <AlertTitle className="flex items-center gap-2">
            Current Period: {monthNames[seasonInfo.currentMonth - 1]} {new Date().getFullYear()}
            {seasonInfo.isBiddingOpen ? (
              <Badge variant="default">Bidding Open</Badge>
            ) : (
              <Badge variant="secondary">Bidding Closed</Badge>
            )}
          </AlertTitle>
          <AlertDescription className="mt-2">
            {seasonInfo.isBiddingOpen ? (
              <div className="space-y-1">
                <p>Bidding period closes in <strong>{seasonInfo.daysUntilClose} days</strong></p>
                <p className="text-sm">
                  Closing Date: {new Date(seasonInfo.biddingCloseDate).toLocaleDateString()}
                </p>
              </div>
            ) : (
              <p>Bidding is currently closed. Next period starts on {new Date(seasonInfo.periodStartDate).toLocaleDateString()}</p>
            )}
          </AlertDescription>
        </Alert>
      )}

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Bids</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{myBids.length}</div>
            <p className="text-xs text-muted-foreground">All your placed bids</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Currently Winning</CardTitle>
            <Trophy className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">{currentWinningBids.length}</div>
            <p className="text-xs text-muted-foreground">Leading bids right now</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">My Possessions</CardTitle>
            <Package className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-blue-600">{possessedSections.length}</div>
            <p className="text-xs text-muted-foreground">Sections you own</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Days Remaining</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{seasonInfo?.daysUntilClose || 0}</div>
            <p className="text-xs text-muted-foreground">Until period closes</p>
          </CardContent>
        </Card>
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>Manage your bidding activity</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4 md:grid-cols-3">
          <Button
            className="w-full"
            onClick={() => navigate('/dashboard/bidding')}
          >
            <TrendingUp className="mr-2 h-4 w-4" />
            Browse Sections
          </Button>
          <Button
            variant="outline"
            className="w-full"
            onClick={() => navigate('/dashboard/my-bids')}
          >
            <Calendar className="mr-2 h-4 w-4" />
            View My Bids
          </Button>
          <Button
            variant="outline"
            className="w-full"
            onClick={() => loadDashboardData()}
          >
            <Loader2 className="mr-2 h-4 w-4" />
            Refresh Data
          </Button>
        </CardContent>
      </Card>

      {/* Current Winning Bids Preview */}
      {currentWinningBids.length > 0 && (
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>Currently Winning Bids</CardTitle>
                <CardDescription>Sections where you have the highest bid</CardDescription>
              </div>
              <Button variant="outline" onClick={() => navigate('/dashboard/my-bids')}>
                View All
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {currentWinningBids.slice(0, 5).map((bid) => (
                <div
                  key={bid.bidId}
                  className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent cursor-pointer"
                  onClick={() => navigate(`/dashboard/bidding/section/${bid.sectionId}`)}
                >
                  <div>
                    <p className="font-medium">{bid.sectionName}</p>
                    <p className="text-sm text-muted-foreground">
                      Placed on {new Date(bid.bidTime).toLocaleDateString()}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-lg font-bold text-green-600">{bid.amount.toFixed(2)} DH</p>
                    <Badge variant="default">Leading</Badge>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Period Information Card */}
      {seasonInfo && (
        <Card>
          <CardHeader>
            <CardTitle>Period Information</CardTitle>
            <CardDescription>Monthly bidding cycle details</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Current Month</Label>
                <p className="font-medium">{monthNames[seasonInfo.currentMonth - 1]}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Period Number</Label>
                <p className="font-medium">Period {seasonInfo.currentPeriod} of 12</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Period Start</Label>
                <p className="font-medium">{new Date(seasonInfo.periodStartDate).toLocaleDateString()}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Period End</Label>
                <p className="font-medium">{new Date(seasonInfo.periodEndDate).toLocaleDateString()}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Bidding Opens</Label>
                <p className="font-medium">{new Date(seasonInfo.biddingOpenDate).toLocaleDateString()}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Bidding Closes</Label>
                <p className="font-medium">{new Date(seasonInfo.biddingCloseDate).toLocaleDateString()}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

function Label({ className, children }: { className?: string; children: React.ReactNode }) {
  return <p className={className}>{children}</p>;
}
