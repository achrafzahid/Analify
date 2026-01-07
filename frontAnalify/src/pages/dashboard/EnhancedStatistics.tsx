import React, { useState, useMemo } from 'react';
import {
  TrendingUp,
  TrendingDown,
  DollarSign,
  Package,
  ShoppingCart,
  Users,
  Award,
  AlertTriangle,
  CheckCircle,
  Info,
  Zap,
  Target,
  Briefcase,
  Store,
  BarChart3,
  PieChart,
  Activity,
  Calendar,
  Flame,
  Lightbulb,
} from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Progress } from '@/components/ui/progress';
import { Skeleton } from '@/components/ui/skeleton';
import { StatCard } from '@/components/shared/StatCard';
import { FilterPanel } from '@/components/shared/FilterPanel';
import { LineChartCard, BarChartCard, PieChartCard, AreaChartCard } from '@/components/charts';
import { useAuth } from '@/contexts/AuthContext';
import { statsApi, type StatisticsFilters } from '@/services/api';

const EnhancedStatistics = () => {
  const { user } = useAuth();
  const role = user?.role;
  const [filters, setFilters] = useState<StatisticsFilters>({});

  const { data: dashboard, isLoading } = useQuery({
    queryKey: ['enhanced-dashboard', filters],
    queryFn: () => statsApi.getEnhancedDashboard(filters),
  });

  const insights = (dashboard as any)?.insights || [];
  const predictions = (dashboard as any)?.predictions;
  const financial = (dashboard as any)?.financialSummary;
  const sectionStats = (dashboard as any)?.sectionStats;
  const investorData = (dashboard as any)?.investorData;
  const adminData = (dashboard as any)?.adminGData;

  // Chart configurations
  const revenueChartData = useMemo(() => {
    if (!(dashboard as any)?.revenueOverTime) return [];
    return (dashboard as any).revenueOverTime.map((point: any) => ({
      date: point.date,
      revenue: point.value,
    }));
  }, [(dashboard as any)?.revenueOverTime]);

  const bidsChartData = useMemo(() => {
    if (!(dashboard as any)?.bidsOverTime) return [];
    return (dashboard as any).bidsOverTime.map((point: any) => ({
      date: point.date,
      bids: point.value,
    }));
  }, [(dashboard as any)?.bidsOverTime]);

  const categoryPieData = useMemo(() => {
    if (!(dashboard as any)?.categoryRevenueDistribution) return [];
    return Object.entries((dashboard as any).categoryRevenueDistribution).map(([name, value]) => ({
      name,
      value: Number(value),
    }));
  }, [(dashboard as any)?.categoryRevenueDistribution]);

  const weeklyActivityData = useMemo(() => {
    if (!(dashboard as any)?.ordersByDayOfWeek) return [];
    return Object.entries((dashboard as any).ordersByDayOfWeek).map(([day, count]) => ({
      day: day.substring(0, 3),
      orders: Number(count),
    }));
  }, [(dashboard as any)?.ordersByDayOfWeek]);

  const getInsightIcon = (type: string) => {
    switch (type) {
      case 'WARNING': return <AlertTriangle className="h-5 w-5 text-yellow-500" />;
      case 'SUCCESS': return <CheckCircle className="h-5 w-5 text-green-500" />;
      case 'OPPORTUNITY': return <Lightbulb className="h-5 w-5 text-blue-500" />;
      default: return <Info className="h-5 w-5 text-gray-500" />;
    }
  };

  const getInsightVariant = (type: string): "default" | "destructive" => {
    return type === 'WARNING' ? 'destructive' : 'default';
  };

  if (isLoading) {
    return (
      <div className="space-y-6 p-6">
        <Skeleton className="h-12 w-64" />
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {[...Array(8)].map((_, i) => (
            <Skeleton key={i} className="h-32" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Analytics Dashboard</h1>
          <p className="text-muted-foreground">
            Comprehensive insights and predictions for your business
          </p>
        </div>
      </div>

      {/* Insights Banner */}
      {insights.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {insights.slice(0, 3).map((insight: any, index: number) => (
            <Alert key={index} variant={getInsightVariant(insight.type)}>
              <div className="flex items-start gap-2">
                {getInsightIcon(insight.type)}
                <div>
                  <AlertTitle>{insight.title}</AlertTitle>
                  <AlertDescription className="text-sm mt-1">
                    {insight.description}
                  </AlertDescription>
                  {insight.actionRecommendation && (
                    <p className="text-xs font-medium mt-2 text-primary">
                      → {insight.actionRecommendation}
                    </p>
                  )}
                </div>
              </div>
            </Alert>
          ))}
        </div>
      )}

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="sections">Sections & Bidding</TabsTrigger>
          <TabsTrigger value="financial">Financial</TabsTrigger>
          <TabsTrigger value="predictions">Predictions</TabsTrigger>
          {role === 'INVESTOR' && <TabsTrigger value="portfolio">My Portfolio</TabsTrigger>}
          {role === 'ADMIN_G' && <TabsTrigger value="platform">Platform Admin</TabsTrigger>}
        </TabsList>

        {/* OVERVIEW TAB */}
        <TabsContent value="overview" className="space-y-4">
          {/* Core KPIs */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <StatCard
              title="Total Revenue"
              value={`$${(dashboard as any)?.totalRevenue?.toLocaleString() || '0'}`}
              icon={DollarSign}
              trend={financial?.revenueGrowthRate ? {
                value: financial.revenueGrowthRate,
                isPositive: financial.revenueGrowthRate > 0
              } : undefined}
              description="vs last month"
            />
            <StatCard
              title="Stock Value"
              value={`$${(dashboard as any)?.totalStockValue?.toLocaleString() || '0'}`}
              icon={Package}
              description={`${(dashboard as any)?.lowStockCount || 0} low stock`}
            />
            <StatCard
              title="Total Orders"
              value={(dashboard as any)?.totalOrders?.toLocaleString() || '0'}
              icon={ShoppingCart}
              description={`Avg: $${(dashboard as any)?.averageOrderValue?.toFixed(2) || '0'}`}
            />
            <StatCard
              title="Active Sections"
              value={(dashboard as any)?.activeBiddingSections?.toString() || '0'}
              icon={Flame}
              description={`${(dashboard as any)?.totalBids || 0} total bids`}
            />
          </div>

          {/* Financial Health */}
          {financial && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Activity className="h-5 w-5" />
                  Financial Health
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Profit Margin</p>
                    <div className="flex items-baseline gap-2 mt-2">
                      <span className="text-3xl font-bold">{financial.profitMargin?.toFixed(1)}%</span>
                      <Badge variant={financial.profitMargin > 25 ? "default" : "secondary"}>
                        {financial.financialHealth}
                      </Badge>
                    </div>
                    <Progress value={financial.profitMargin} className="mt-2" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">ROI</p>
                    <div className="flex items-baseline gap-2 mt-2">
                      <span className="text-3xl font-bold">{financial.roi?.toFixed(1)}%</span>
                      {financial.roi > 20 && (
                        <TrendingUp className="h-5 w-5 text-green-500" />
                      )}
                    </div>
                    <Progress value={Math.min(financial.roi, 100)} className="mt-2" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Gross Profit</p>
                    <div className="mt-2">
                      <span className="text-3xl font-bold">
                        ${financial.grossProfit?.toLocaleString()}
                      </span>
                    </div>
                    <p className="text-xs text-muted-foreground mt-2">
                      Expected: ${financial.expectedIncome?.toLocaleString()}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Charts Row */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <AreaChartCard
              title="Revenue Trend"
              description="Revenue over time"
              data={revenueChartData}
              xKey="date"
              yKeys={['revenue']}
              colors={['hsl(217, 91%, 60%)']}
            />
            <LineChartCard
              title="Weekly Activity"
              description="Orders by day of week"
              data={weeklyActivityData}
              xKey="day"
              yKeys={['orders']}
              colors={['hsl(142, 76%, 36%)']}
            />
          </div>

          {/* Category Distribution */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <PieChartCard
              title="Revenue by Category"
              description="Category performance breakdown"
              data={categoryPieData}
            />
            <Card>
              <CardHeader>
                <CardTitle>Top Products</CardTitle>
                <CardDescription>Best performing products</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {(dashboard as any)?.topProducts?.slice(0, 5).map((item: any, index: number) => (
                    <div key={index} className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <div className="flex h-6 w-6 items-center justify-center rounded-full bg-primary/10 text-xs font-bold">
                          {index + 1}
                        </div>
                        <span className="text-sm font-medium">{item.name}</span>
                      </div>
                      <span className="text-sm font-bold">${item.value?.toFixed(2)}</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* SECTIONS & BIDDING TAB */}
        <TabsContent value="sections" className="space-y-4">
          {sectionStats && (
            <>
              {/* Section KPIs */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <StatCard
                  title="Total Sections"
                  value={sectionStats.totalSections?.toString() || '0'}
                  icon={Target}
                  description={`${sectionStats.activeSections || 0} active`}
                />
                <StatCard
                  title="Section Value"
                  value={`$${sectionStats.totalSectionValue?.toLocaleString() || '0'}`}
                  icon={Award}
                  description="Total value"
                />
                <StatCard
                  title="Total Bids"
                  value={sectionStats.totalBids?.toString() || '0'}
                  icon={Zap}
                  description={`Avg: ${sectionStats.averageBidsPerSection?.toFixed(1)} per section`}
                />
                <StatCard
                  title="Bid Win Rate"
                  value={`${sectionStats.bidWinRate?.toFixed(1) || '0'}%`}
                  icon={TrendingUp}
                  trend={sectionStats.bidWinRate > 50 ? {
                    value: 15,
                    isPositive: true
                  } : {
                    value: 5,
                    isPositive: false
                  }}
                />
              </div>

              {/* Market Trend Banner */}
              <Alert>
                <Flame className="h-4 w-4" />
                <AlertTitle>Market Trend: {sectionStats.marketTrend}</AlertTitle>
                <AlertDescription>
                  {sectionStats.marketTrend === 'HOT' && 'High bidding activity - competitive market!'}
                  {sectionStats.marketTrend === 'STABLE' && 'Normal market conditions'}
                  {sectionStats.marketTrend === 'COOLING' && 'Lower bidding activity - opportunity to bid'}
                </AlertDescription>
              </Alert>

              {/* Bidding Charts */}
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <LineChartCard
                  title="Bidding Activity"
                  description="Bids over time"
                  data={bidsChartData}
                  xKey="date"
                  yKeys={['bids']}
                  colors={['hsl(262, 83%, 58%)']}
                />
                <Card>
                  <CardHeader>
                    <CardTitle>Section Value by Category</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {Object.entries(sectionStats.valueByCategory || {}).map(([category, value]: [string, any]) => (
                        <div key={category}>
                          <div className="flex justify-between text-sm mb-1">
                            <span>{category}</span>
                            <span className="font-bold">${Number(value).toLocaleString()}</span>
                          </div>
                          <Progress value={sectionStats.totalSectionValue ? (Number(value) / sectionStats.totalSectionValue) * 100 : 0} />
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              </div>

              {/* Leaderboards */}
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Award className="h-5 w-5" />
                      Most Competitive Sections
                    </CardTitle>
                    <CardDescription>Sections with most bids</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {sectionStats.mostCompetitiveSections && sectionStats.mostCompetitiveSections.length > 0 ? (
                        sectionStats.mostCompetitiveSections.slice(0, 5).map((item: any, index: number) => (
                          <div key={index} className="flex items-center justify-between">
                            <div className="flex items-center gap-2">
                              <Badge variant="outline">{index + 1}</Badge>
                              <span className="text-sm">{item.name}</span>
                            </div>
                            <span className="text-sm font-bold">{item.value} bids</span>
                          </div>
                        ))
                      ) : (
                        <p className="text-sm text-muted-foreground">No competitive sections data</p>
                      )}
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Users className="h-5 w-5" />
                      Top Bidders
                    </CardTitle>
                    <CardDescription>Most active investors</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {sectionStats.topBidders && sectionStats.topBidders.length > 0 ? (
                        sectionStats.topBidders.slice(0, 5).map((item: any, index: number) => (
                          <div key={index} className="flex items-center justify-between">
                            <div className="flex items-center gap-2">
                              <div className={`flex h-6 w-6 items-center justify-center rounded-full ${index < 3 ? 'bg-yellow-500/20 text-yellow-700' : 'bg-gray-200'} text-xs font-bold`}>
                                {index + 1}
                              </div>
                              <span className="text-sm">{item.name}</span>
                            </div>
                            <span className="text-sm font-bold">${item.value?.toFixed(2)}</span>
                          </div>
                        ))
                      ) : (
                        <p className="text-sm text-muted-foreground">No bidder data available</p>
                      )}
                    </div>
                  </CardContent>
                </Card>
              </div>
            </>
          )}
        </TabsContent>

        {/* FINANCIAL TAB */}
        <TabsContent value="financial" className="space-y-4">
          {financial && (
            <>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <Card>
                  <CardHeader>
                    <CardTitle className="text-sm">Revenue Streams</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-2">
                    <div>
                      <p className="text-xs text-muted-foreground">Product Sales</p>
                      <p className="text-2xl font-bold">${financial.productSalesRevenue?.toLocaleString()}</p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Section Sales</p>
                      <p className="text-2xl font-bold">${financial.sectionSalesRevenue?.toLocaleString()}</p>
                    </div>
                    <div className="pt-2 border-t">
                      <p className="text-xs text-muted-foreground">Total Revenue</p>
                      <p className="text-3xl font-bold text-primary">${financial.totalRevenue?.toLocaleString()}</p>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-sm">Costs & Investments</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-2">
                    <div>
                      <p className="text-xs text-muted-foreground">Stock Cost</p>
                      <p className="text-2xl font-bold">${financial.totalStockCost?.toLocaleString()}</p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Section Investments</p>
                      <p className="text-2xl font-bold">${financial.totalSectionInvestments?.toLocaleString()}</p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Operational Costs</p>
                      <p className="text-2xl font-bold">${financial.operationalCosts?.toLocaleString()}</p>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-sm">Cash Flow</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-2">
                    <div>
                      <p className="text-xs text-muted-foreground">Expected Income</p>
                      <p className="text-2xl font-bold text-green-600">${financial.expectedIncome?.toLocaleString()}</p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Pending Payments</p>
                      <p className="text-2xl font-bold text-yellow-600">${financial.pendingPayments?.toLocaleString()}</p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Available Cash</p>
                      <p className="text-2xl font-bold text-blue-600">${financial.availableCash?.toLocaleString()}</p>
                    </div>
                  </CardContent>
                </Card>
              </div>

              <Card>
                <CardHeader>
                  <CardTitle>Performance Metrics</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <div className="flex justify-between items-center mb-2">
                        <span className="text-sm font-medium">Profit Margin</span>
                        <span className="text-lg font-bold">{financial.profitMargin?.toFixed(2)}%</span>
                      </div>
                      <Progress value={financial.profitMargin} />
                      <p className="text-xs text-muted-foreground mt-1">
                        Target: 25% | Current: {financial.profitMargin > 25 ? 'Above' : 'Below'} target
                      </p>
                    </div>
                    <div>
                      <div className="flex justify-between items-center mb-2">
                        <span className="text-sm font-medium">Return on Investment</span>
                        <span className="text-lg font-bold">{financial.roi?.toFixed(2)}%</span>
                      </div>
                      <Progress value={Math.min(financial.roi, 100)} />
                      <p className="text-xs text-muted-foreground mt-1">
                        Industry avg: 20% | You: {financial.roi > 20 ? 'Outperforming' : 'Below average'}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </>
          )}
        </TabsContent>

        {/* PREDICTIONS TAB */}
        <TabsContent value="predictions" className="space-y-4">
          {predictions && (
            <>
              {/* Prediction Cards */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <Card>
                  <CardHeader>
                    <CardTitle className="text-sm flex items-center gap-2">
                      <Calendar className="h-4 w-4" />
                      Next Month Revenue
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-3xl font-bold">${predictions.predictedNextMonthRevenue?.toLocaleString()}</p>
                    <div className="flex items-center gap-2 mt-2">
                      <Progress value={predictions.confidence * 100} className="flex-1" />
                      <span className="text-xs text-muted-foreground">{(predictions.confidence * 100).toFixed(0)}% confident</span>
                    </div>
                    <Badge className="mt-2" variant={predictions.overallTrend === 'BULLISH' ? 'default' : 'secondary'}>
                      {predictions.overallTrend}
                    </Badge>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-sm flex items-center gap-2">
                      <BarChart3 className="h-4 w-4" />
                      Next Quarter Revenue
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-3xl font-bold">${predictions.predictedNextQuarterRevenue?.toLocaleString()}</p>
                    <p className="text-xs text-muted-foreground mt-2">Based on current trends</p>
                    <div className="flex items-center gap-1 mt-2">
                      {predictions.overallTrend === 'BULLISH' ? (
                        <TrendingUp className="h-4 w-4 text-green-500" />
                      ) : (
                        <TrendingDown className="h-4 w-4 text-red-500" />
                      )}
                      <span className="text-sm font-medium">
                        {predictions.overallTrend === 'BULLISH' ? 'Growing' : predictions.overallTrend === 'BEARISH' ? 'Declining' : 'Stable'}
                      </span>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-sm flex items-center gap-2">
                      <Activity className="h-4 w-4" />
                      Bidding Activity
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-3xl font-bold">{predictions.predictedBiddingActivity?.toFixed(0)}</p>
                    <p className="text-xs text-muted-foreground mt-2">Predicted bids</p>
                    <Badge className="mt-2" variant="outline">
                      {predictions.seasonalityFactor}
                    </Badge>
                  </CardContent>
                </Card>
              </div>

              {/* Stock Recommendations */}
              {predictions.stockRecommendations && predictions.stockRecommendations.length > 0 && (
                <Card>
                  <CardHeader>
                    <CardTitle>Stock Recommendations</CardTitle>
                    <CardDescription>AI-powered restocking suggestions</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {predictions.stockRecommendations.slice(0, 5).map((rec: any, index: number) => (
                        <div key={index} className="flex items-center justify-between p-3 border rounded-lg">
                          <div className="flex-1">
                            <p className="font-medium">{rec.productName}</p>
                            <p className="text-xs text-muted-foreground">{rec.reason}</p>
                            <div className="flex items-center gap-2 mt-2">
                              <Badge variant={rec.priority === 'HIGH' ? 'destructive' : rec.priority === 'MEDIUM' ? 'default' : 'secondary'} className="text-xs">
                                {rec.priority}
                              </Badge>
                              <Badge variant="outline" className="text-xs">{rec.action}</Badge>
                            </div>
                          </div>
                          <div className="text-right">
                            <p className="text-sm text-muted-foreground">Current: {rec.currentStock}</p>
                            <p className="text-sm font-bold">Recommend: {rec.recommendedStock}</p>
                            <p className="text-xs text-muted-foreground">${rec.estimatedCost?.toFixed(2)}</p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Opportunities & Risks */}
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-green-600">
                      <Lightbulb className="h-5 w-5" />
                      Opportunities
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <ul className="space-y-2">
                      {predictions.opportunities && predictions.opportunities.length > 0 ? (
                        predictions.opportunities.map((opp: string, index: number) => (
                          <li key={index} className="flex items-start gap-2">
                            <CheckCircle className="h-4 w-4 text-green-500 mt-0.5" />
                            <span className="text-sm">{opp}</span>
                          </li>
                        ))
                      ) : (
                        <li className="text-sm text-muted-foreground">No opportunities identified</li>
                      )}
                    </ul>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-yellow-600">
                      <AlertTriangle className="h-5 w-5" />
                      Risks to Monitor
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <ul className="space-y-2">
                      {predictions.risks && predictions.risks.length > 0 ? (
                        predictions.risks.map((risk: string, index: number) => (
                          <li key={index} className="flex items-start gap-2">
                            <AlertTriangle className="h-4 w-4 text-yellow-500 mt-0.5" />
                            <span className="text-sm">{risk}</span>
                          </li>
                        ))
                      ) : (
                        <li className="text-sm text-muted-foreground">No risks identified</li>
                      )}
                    </ul>
                  </CardContent>
                </Card>
              </div>

              {/* Hot Sections */}
              {predictions.hotSectionsToWatch && predictions.hotSectionsToWatch.length > 0 && (
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Flame className="h-5 w-5 text-orange-500" />
                      Hot Sections to Watch
                    </CardTitle>
                    <CardDescription>High-activity sections worth monitoring</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="flex flex-wrap gap-2">
                      {predictions.hotSectionsToWatch.map((section: string, index: number) => (
                        <Badge key={index} variant="outline" className="text-sm">
                          {section}
                        </Badge>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}
            </>
          )}
        </TabsContent>

        {/* INVESTOR PORTFOLIO TAB */}
        {role === 'INVESTOR' && investorData && (
          <TabsContent value="portfolio" className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <StatCard
                title="Products Owned"
                value={investorData.totalProductsOwned?.toString() || '0'}
                icon={Package}
              />
              <StatCard
                title="Portfolio Value"
                value={`$${investorData.portfolioValue?.toLocaleString() || '0'}`}
                icon={Briefcase}
                trend={investorData.portfolioGrowth ? {
                  value: investorData.portfolioGrowth,
                  isPositive: investorData.portfolioGrowth > 0
                } : undefined}
              />
              <StatCard
                title="Sections Won"
                value={investorData.totalSectionsWon?.toString() || '0'}
                icon={Award}
              />
              <StatCard
                title="Active Bids"
                value={investorData.activeBids?.toString() || '0'}
                icon={Zap}
                description={`$${investorData.totalBidAmount?.toLocaleString() || '0'} total`}
              />
            </div>

            <Card>
              <CardHeader>
                <CardTitle>Performance Rating</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex items-center gap-4">
                  <div className="flex-1">
                    <Badge variant="default" className="text-lg px-4 py-2">
                      {investorData.performanceRating}
                    </Badge>
                  </div>
                  <div className="text-right">
                    <p className="text-sm text-muted-foreground">Portfolio Growth</p>
                    <p className="text-2xl font-bold text-green-600">
                      +{investorData.portfolioGrowth?.toFixed(1)}%
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <Card>
                <CardHeader>
                  <CardTitle>Best Selling Products</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {investorData.bestSellingProducts && investorData.bestSellingProducts.length > 0 ? (
                      investorData.bestSellingProducts.map((product: any, index: number) => (
                        <div key={index} className="flex justify-between items-center">
                          <span className="text-sm">{product.name}</span>
                          <span className="font-bold">${product.value?.toFixed(2)}</span>
                        </div>
                      ))
                    ) : (
                      <p className="text-sm text-muted-foreground">No sales data available</p>
                    )}
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Recommendations</CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2">
                    {investorData.recommendations && investorData.recommendations.length > 0 ? (
                      investorData.recommendations.map((rec: string, index: number) => (
                        <li key={index} className="flex items-start gap-2">
                          <Lightbulb className="h-4 w-4 text-blue-500 mt-0.5" />
                          <span className="text-sm">{rec}</span>
                        </li>
                      ))
                    ) : (
                      <li className="text-sm text-muted-foreground">No recommendations available</li>
                    )}
                  </ul>
                </CardContent>
              </Card>
            </div>
          </TabsContent>
        )}

        {/* ADMIN-G PLATFORM TAB */}
        {role === 'ADMIN_G' && adminData && (
          <TabsContent value="platform" className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <StatCard
                title="Total Users"
                value={adminData.totalUsers?.toString() || '0'}
                icon={Users}
              />
              <StatCard
                title="Platform Revenue"
                value={`$${adminData.platformRevenue?.toLocaleString() || '0'}`}
                icon={DollarSign}
                trend={adminData.platformGrowthRate ? {
                  value: adminData.platformGrowthRate,
                  isPositive: adminData.platformGrowthRate > 0
                } : undefined}
              />
              <StatCard
                title="Total Stores"
                value={adminData.totalStores?.toString() || '0'}
                icon={Store}
              />
              <StatCard
                title="Total Investors"
                value={adminData.totalInvestors?.toString() || '0'}
                icon={Briefcase}
              />
            </div>

            <Card>
              <CardHeader>
                <CardTitle>System Health</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <Badge variant={adminData.overallSystemHealth === 'HEALTHY' ? 'default' : 'destructive'} className="text-lg px-4 py-2">
                    {adminData.overallSystemHealth}
                  </Badge>
                  <div>
                    <p className="text-sm text-muted-foreground">Critical Items</p>
                    <p className="text-xl font-bold">{adminData.criticalLowStockItems || 0}</p>
                  </div>
                </div>
                {adminData.systemAlerts && (
                  <div className="mt-4 space-y-1">
                    {adminData.systemAlerts.map((alert: string, index: number) => (
                      <p key={index} className="text-sm text-muted-foreground">✓ {alert}</p>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        )}
      </Tabs>
    </div>
  );
};

export default EnhancedStatistics;
