package org.home.test.task.tracker.api.controllers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.home.test.task.tracker.api.controllers.helpers.ControllerHelper;
import org.home.test.task.tracker.api.dto.AckDTO;
import org.home.test.task.tracker.api.dto.ProjectDTO;
import org.home.test.task.tracker.api.exceptions.BadRequestException;
import org.home.test.task.tracker.api.factories.ProjectFactoryDTO;
import org.home.test.task.tracker.store.entities.ProjectEntity;
import org.home.test.task.tracker.store.repositories.ProjectRepository;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@RestController
public class ProjectController {

    ProjectRepository projectRepository;
    ProjectFactoryDTO projectFactoryDTO;
    ControllerHelper controllerHelper;

    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";
    public static final String FETCH_PROJECTS = "/api/projects";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";

    @GetMapping(FETCH_PROJECTS)
    public List<ProjectDTO> fetchProjects(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.isBlank());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAllBy);

        return projectStream.map(projectFactoryDTO::buildProjectDTO).collect(Collectors.toList());
    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDTO createOrUpdateProject(
            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName,
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId) {

        optionalProjectName = optionalProjectName.filter(projectName -> !projectName.isBlank());

        boolean isProjectIdExist = optionalProjectId.isPresent();
        boolean isProjectNameExist = optionalProjectName.isPresent();

        if (!isProjectIdExist && !isProjectNameExist) {
            throw new BadRequestException("Project name can't be empty");
        }

        ProjectEntity project = optionalProjectId
                .map(controllerHelper::getProject)
                .orElseGet(() -> ProjectEntity.builder().build());

        optionalProjectName.ifPresent(projectName -> {
            projectRepository.findByName(projectName)
                    .filter(x_project -> !Objects.equals(x_project.getId(), project.getId()))
                    .ifPresent(x_project -> {
                        throw new BadRequestException(String.format("Project \"%s\" already exists!", projectName));
                    });
            project.setName(projectName);
        });

        final ProjectEntity savedProject = projectRepository.saveAndFlush(project);

        return projectFactoryDTO.buildProjectDTO(savedProject);
    }

    @DeleteMapping(DELETE_PROJECT)
    public AckDTO deleteProject(@PathVariable("project_id") Long projectId) {

        controllerHelper.getProject(projectId);
        projectRepository.deleteById(projectId);

        return AckDTO.makeDefault(true);
    }
}
