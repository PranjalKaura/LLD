package LiveFeature;
import java.util.*;

public class FeatureLive {
    public static void main(String[] args) {
        FeatureManager featureManager = FeatureManager.getInstance();

        Feature mainFeature1 = FeatureFactory.createFeature(FeatureType.MAIN_FEAUTURE);
        Feature subFeature1_1 = FeatureFactory.createFeature(FeatureType.SUB_FEATURE);
        Feature subFeature1_2 = FeatureFactory.createFeature(FeatureType.SUB_FEATURE);
        Feature subFeature1_3 = FeatureFactory.createFeature(FeatureType.SUB_FEATURE);
        mainFeature1.addSubFeature(subFeature1_1); 
        mainFeature1.addSubFeature(subFeature1_2);
        mainFeature1.addSubFeature(subFeature1_3);

        Feature mainFeature2 = FeatureFactory.createFeature(FeatureType.MAIN_FEAUTURE);
        Feature subFeature2_1 = FeatureFactory.createFeature(FeatureType.SUB_FEATURE);
        mainFeature2.addSubFeature(subFeature2_1); 

        featureManager.addBaseFeature(mainFeature1);
        featureManager.addBaseFeature(mainFeature2);

        featureManager.checkFeatureAliveStatus();

        System.out.println("Base feature parity score: " + featureManager.getParityScore());
        System.out.println("Feature " + mainFeature1.id  + " " + mainFeature1.getParityScore());
        System.out.println("Feature " + mainFeature2.id  + " " + mainFeature2.getParityScore());
    }
}

class FeatureManager
{
    List<Feature> baseFeatures;
    static FeatureManager featureManagerInstance;

    private FeatureManager()
    {
        baseFeatures = new ArrayList<>();
    }

    public static FeatureManager getInstance()
    {
        if(FeatureManager.featureManagerInstance==null) FeatureManager.featureManagerInstance = new FeatureManager();
        return FeatureManager.featureManagerInstance;
    }

    public void addBaseFeature(Feature feature)
    {
        baseFeatures.add(feature);
    }

    public double getParityScore()
    {
        double countLive = 0;
        for(Feature feature:baseFeatures)
        {
            if(feature.isLive()) countLive++;
        }
        return countLive/baseFeatures.size();
    }

    public void checkFeatureAliveStatus()
    {
        for(Feature feature:baseFeatures)
        {
            System.out.println("Feature " + feature.id + " active => " + feature.isLive());
        }
    }
}

class FeatureFactory
{
    public static Feature createFeature(FeatureType featureType)
    {
        switch (featureType) {
            case FeatureType.MAIN_FEAUTURE:
                return new Feature(new DefaultCheckFeatureLive());
            case FeatureType.SUB_FEATURE:
                return new Feature(new DefaultCheckSubFeatureLive());
            default:
                return new Feature(new DefaultCheckSubFeatureLive());
        }
    }
}

class Feature
{
    String id;
    List<Feature> subFeatures;
    ICheckFeatureLiveStrategy featureLiveStrategy;

    public Feature(ICheckFeatureLiveStrategy featureLiveStrategy) 
    {
        this.id = UUID.randomUUID().toString();
        this.subFeatures = new ArrayList<>();
        this.featureLiveStrategy = featureLiveStrategy;
    }

    public void addSubFeature(Feature feature)
    {
        this.subFeatures.add(feature);
    }

    public boolean isLive()
    {
        return featureLiveStrategy.checkFeatureLive(this);
    }

    public int getTotalSubFeatures()
    {
        int count = 1;
        for(Feature feature:subFeatures)
        {
            count+=feature.getTotalSubFeatures();
        }
        return count;
    }

    public int getTotalLiveSubFeatures()
    {
        int count = this.isLive()?1:0;
        for(Feature feature:subFeatures)
        {
            count+=feature.getTotalLiveSubFeatures();
        }
        return count;
    }

    public double getParityScore()
    {
        return ((double)this.getTotalLiveSubFeatures())/this.getTotalSubFeatures();
    }
}

interface ICheckFeatureLiveStrategy
{
    public boolean checkFeatureLive(Feature feature);
}

class DefaultCheckFeatureLive implements ICheckFeatureLiveStrategy
{
    public boolean checkFeatureLive(Feature feature)
    {
        if(feature.subFeatures.size()<2) return false;
        boolean firstSubFeatureLive = feature.subFeatures.get(0).isLive();
        boolean secondSubFeatureLive = feature.subFeatures.get(1).isLive();
        return firstSubFeatureLive && secondSubFeatureLive;
    }
}

class DefaultCheckSubFeatureLive implements ICheckFeatureLiveStrategy
{
    public boolean checkFeatureLive(Feature feature)
    {
        return true;
    }
}


enum FeatureType
{
    MAIN_FEAUTURE,
    SUB_FEATURE
}
