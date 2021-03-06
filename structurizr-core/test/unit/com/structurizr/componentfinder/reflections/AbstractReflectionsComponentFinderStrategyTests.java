package com.structurizr.componentfinder.reflections;

import com.structurizr.Workspace;
import com.structurizr.componentfinder.*;
import com.structurizr.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AbstractReflectionsComponentFinderStrategyTests {

    private Container webApplication;

    @Before
    public void setUp() {
        Workspace workspace = new Workspace("Name", "Description");
        Model model = workspace.getModel();

        SoftwareSystem softwareSystem = model.addSoftwareSystem("Name", "Description");
        webApplication = softwareSystem.addContainer("Name", "Description", "Technology");
    }

    @Test
    public void test_findComponents_DoesNotBreak_WhenThereIsACyclicDependency() throws Exception {
        ComponentFinder componentFinder = new ComponentFinder(
                webApplication,
                "com.structurizr.componentfinder.reflections.cyclicDependency",
                new TypeBasedComponentFinderStrategy(
                        new NameSuffixTypeMatcher("Component", "", "")
                )
        );
        componentFinder.findComponents();

        assertEquals(2, webApplication.getComponents().size());

        Component aComponent = webApplication.getComponentWithName("AComponent");
        assertNotNull(aComponent);
        assertEquals("AComponent", aComponent.getName());
        assertEquals("com.structurizr.componentfinder.reflections.cyclicDependency.AComponent", aComponent.getType());

        Component bComponent = webApplication.getComponentWithName("BComponent");
        assertNotNull(bComponent);
        assertEquals("BComponent", bComponent.getName());
        assertEquals("com.structurizr.componentfinder.reflections.cyclicDependency.BComponent", bComponent.getType());

        assertEquals(1, aComponent.getRelationships().size());
        assertNotNull(aComponent.getRelationships().stream().filter(r -> r.getDestination() == bComponent).findFirst().get());

        assertEquals(1, bComponent.getRelationships().size());
        assertNotNull(bComponent.getRelationships().stream().filter(r -> r.getDestination() == aComponent).findFirst().get());
    }

    @Test
    public void test_findComponents_CorrectlyFindsDependenciesFromSuperclass() throws Exception {
        ComponentFinder componentFinder = new ComponentFinder(
                webApplication,
                "com.structurizr.componentfinder.reflections.dependenciesFromSuperClass",
                new TypeBasedComponentFinderStrategy(
                        new NameSuffixTypeMatcher("Component", "", "")
                )
        );
        componentFinder.findComponents();

        assertEquals(2, webApplication.getComponents().size());

        Component someComponent = webApplication.getComponentWithName("SomeComponent");
        assertNotNull(someComponent);
        assertEquals("SomeComponent", someComponent.getName());
        assertEquals("com.structurizr.componentfinder.reflections.dependenciesFromSuperClass.SomeComponent", someComponent.getType());

        Component loggingComponent = webApplication.getComponentWithName("LoggingComponent");
        assertNotNull(loggingComponent);
        assertEquals("LoggingComponent", loggingComponent.getName());
        assertEquals("com.structurizr.componentfinder.reflections.dependenciesFromSuperClass.LoggingComponent", loggingComponent.getType());

        assertEquals(1, someComponent.getRelationships().size());
        assertNotNull(someComponent.getRelationships().stream().filter(r -> r.getDestination() == loggingComponent).findFirst().get());
    }

    @Test
    public void test_findComponents_CorrectlyFindsNoDependenciesWhenTwoComponentsImplementTheSameInterface() throws Exception {
        ComponentFinder componentFinder = new ComponentFinder(
                webApplication,
                "com.structurizr.componentfinder.reflections.featureinterface",
                new TypeBasedComponentFinderStrategy(
                        new NameSuffixTypeMatcher("Component", "", "")
                )
        );
        componentFinder.findComponents();

        assertEquals(2, webApplication.getComponents().size());

        Component someComponent = webApplication.getComponentWithName("SomeComponent");
        assertNotNull(someComponent);
        assertEquals("SomeComponent", someComponent.getName());
        assertEquals("com.structurizr.componentfinder.reflections.featureinterface.SomeComponent", someComponent.getType());

        Component otherComponent = webApplication.getComponentWithName("OtherComponent");
        assertNotNull(otherComponent);
        assertEquals("OtherComponent", otherComponent.getName());
        assertEquals("com.structurizr.componentfinder.reflections.featureinterface.OtherComponent", otherComponent.getType());

        assertEquals(0, someComponent.getRelationships().size());
        assertEquals(0, otherComponent.getRelationships().size());
    }

    @Test
    public void test_findComponents_CorrectlyFindsDependenciesBetweenComponentsFoundByDifferentComponentFinders_WhenPackage1IsScannedFirst() throws Exception {
        ComponentFinder componentFinder1 = new ComponentFinder(
                webApplication,
                "com.structurizr.componentfinder.reflections.multipleComponentFinders.package1",
                new TypeBasedComponentFinderStrategy(
                        new NameSuffixTypeMatcher("Controller", "", "")
                )
        );

        ComponentFinder componentFinder2 = new ComponentFinder(
                webApplication,
                "com.structurizr.componentfinder.reflections.multipleComponentFinders.package2",
                new TypeBasedComponentFinderStrategy(
                        new NameSuffixTypeMatcher("Repository", "", "")
                )
        );

        componentFinder1.findComponents();
        componentFinder2.findComponents();

        assertEquals(2, webApplication.getComponents().size());
        Component myController = webApplication.getComponentWithName("MyController");
        Component myRepository = webApplication.getComponentWithName("MyRepository");
        assertEquals(1, myController.getRelationships().size());
        assertNotNull(myController.getRelationships().stream().filter(r -> r.getDestination() == myRepository).findFirst().get());
    }

    @Test
    public void test_findComponents_CorrectlyFindsDependenciesBetweenComponentsFoundByDifferentComponentFinders_WhenPackage2IsScannedFirst() throws Exception {
        ComponentFinder componentFinder1 = new ComponentFinder(
                webApplication,
                "com.structurizr.componentfinder.reflections.multipleComponentFinders.package1",
                new TypeBasedComponentFinderStrategy(
                        new NameSuffixTypeMatcher("Controller", "", "")
                )
        );

        ComponentFinder componentFinder2 = new ComponentFinder(
                webApplication,
                "com.structurizr.componentfinder.reflections.multipleComponentFinders.package2",
                new TypeBasedComponentFinderStrategy(
                        new NameSuffixTypeMatcher("Repository", "", "")
                )
        );

        componentFinder2.findComponents();
        componentFinder1.findComponents();

        assertEquals(2, webApplication.getComponents().size());
        Component myController = webApplication.getComponentWithName("MyController");
        Component myRepository = webApplication.getComponentWithName("MyRepository");
        assertEquals(1, myController.getRelationships().size());
        assertNotNull(myController.getRelationships().stream().filter(r -> r.getDestination() == myRepository).findFirst().get());
    }

    @Test
    public void test_findComponents_CorrectlyFindsSupportingTypes_WhenTheDefaultStrategyIsUsed() throws Exception {
        ComponentFinder componentFinder = new ComponentFinder(
                webApplication,
                "com.structurizr.componentfinder.reflections.supportingTypes.myapp",
                new StructurizrAnnotationsComponentFinderStrategy()
        );
        componentFinder.findComponents();

        assertEquals(2, webApplication.getComponents().size());
        Component myController = webApplication.getComponentWithName("MyController");
        Component myRepository = webApplication.getComponentWithName("MyRepository");
        assertEquals(1, myController.getRelationships().size());
        assertNotNull(myController.getRelationships().stream().filter(r -> r.getDestination() == myRepository).findFirst().get());

        // the default strategy for supporting types is to find the first implementation
        // class if the component type is an interface
        assertEquals(1, myController.getCode().size());
        assertCodeElementInComponent(myController, "com.structurizr.componentfinder.reflections.supportingTypes.myapp.web.MyController", CodeElementRole.Primary);

        assertEquals(2, myRepository.getCode().size());
        assertCodeElementInComponent(myRepository, "com.structurizr.componentfinder.reflections.supportingTypes.data.MyRepository", CodeElementRole.Primary);
        assertCodeElementInComponent(myRepository, "com.structurizr.componentfinder.reflections.supportingTypes.data.MyRepositoryImpl", CodeElementRole.Supporting);
    }

    @Test
    public void test_findComponents_CorrectlyFindsSupportingTypes_WhenTheComponentPackageStrategyIsUsed() throws Exception {
        ComponentFinder componentFinder = new ComponentFinder(
                webApplication,
                "com.structurizr.componentfinder.reflections.supportingTypes.myapp",
                new StructurizrAnnotationsComponentFinderStrategy(
                        new ComponentPackageSupportingTypesStrategy()
                )
        );
        componentFinder.findComponents();

        assertEquals(2, webApplication.getComponents().size());
        Component myController = webApplication.getComponentWithName("MyController");
        Component myRepository = webApplication.getComponentWithName("MyRepository");
        assertEquals(1, myController.getRelationships().size());
        assertNotNull(myController.getRelationships().stream().filter(r -> r.getDestination() == myRepository).findFirst().get());

        assertEquals(1, myController.getCode().size());
        assertEquals(3, myRepository.getCode().size());
        assertCodeElementInComponent(myRepository, "com.structurizr.componentfinder.reflections.supportingTypes.myapp.data.MyRepository", CodeElementRole.Primary);
        assertCodeElementInComponent(myRepository, "com.structurizr.componentfinder.reflections.supportingTypes.myapp.data.MyRepositoryImpl", CodeElementRole.Supporting);
        assertCodeElementInComponent(myRepository, "com.structurizr.componentfinder.reflections.supportingTypes.myapp.data.MyRepositoryRowMapper", CodeElementRole.Supporting);
    }

    @Test
    public void test_findComponents_CorrectlyFindsSupportingTypes_WhenTheReferencedTypesStrategyIsUsed() throws Exception {
        ComponentFinder componentFinder = new ComponentFinder(
                webApplication,
                "com.structurizr.componentfinder.reflections.supportingTypes.myapp",
                new StructurizrAnnotationsComponentFinderStrategy(
                        new FirstImplementationOfInterfaceSupportingTypesStrategy(),
                        new ReferencedTypesSupportingTypesStrategy()
                )
        );
        componentFinder.findComponents();

        assertEquals(2, webApplication.getComponents().size());
        Component myController = webApplication.getComponentWithName("MyController");
        Component myRepository = webApplication.getComponentWithName("MyRepository");
        assertEquals(1, myController.getRelationships().size());
        assertNotNull(myController.getRelationships().stream().filter(r -> r.getDestination() == myRepository).findFirst().get());

        assertEquals(2, myController.getCode().size());
        assertCodeElementInComponent(myController, "com.structurizr.componentfinder.reflections.supportingTypes.myapp.MyController", CodeElementRole.Primary);
        assertCodeElementInComponent(myController, "com.structurizr.componentfinder.reflections.supportingTypes.myapp.AbstractComponent", CodeElementRole.Supporting);

        assertEquals(4, myRepository.getCode().size());
        assertCodeElementInComponent(myController, "com.structurizr.componentfinder.reflections.supportingTypes.myapp.data.MyRepository", CodeElementRole.Primary);
        assertCodeElementInComponent(myController, "com.structurizr.componentfinder.reflections.supportingTypes.myapp.AbstractComponent", CodeElementRole.Supporting);
        assertCodeElementInComponent(myController, "com.structurizr.componentfinder.reflections.supportingTypes.myapp.data.MyRepositoryImpl", CodeElementRole.Supporting);
        assertCodeElementInComponent(myController, "com.structurizr.componentfinder.reflections.supportingTypes.myapp.data.MyRepositoryRowMapper", CodeElementRole.Supporting);
    }

    private boolean assertCodeElementInComponent(Component component, String type, CodeElementRole role) {
        for (CodeElement codeElement : component.getCode()) {
            if (codeElement.getType().equals(type)) {
                return codeElement.getRole() == role;
            }
        }

        return false;
    }

}
